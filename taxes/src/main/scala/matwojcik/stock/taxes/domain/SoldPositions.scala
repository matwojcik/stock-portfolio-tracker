package matwojcik.stock.taxes.domain

import cats.Applicative
import cats.data.NonEmptyList
import cats.syntax.all._
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.taxes.domain.Income.SoldPosition

import java.time.Year
import java.time.ZoneId

trait SoldPositions[F[_]] {
  def findSoldPositions(year: Year, zone: ZoneId, transactions: NonEmptyList[Transaction]): F[List[SoldPosition]]
}

object SoldPositions {
  def apply[F[_]](using ev: SoldPositions[F]): SoldPositions[F] = ev

  def instance[F[_]: Applicative]: SoldPositions[F] =
    new SoldPositions[F] {

      override def findSoldPositions(year: Year, zone: ZoneId, transactions: NonEmptyList[Transaction]): F[List[SoldPosition]] = {
        // todo find a way to make sure that all transactions have unique id
        val sortedTransactions = transactions.sortBy(_.date.toEpochMilli)

        val result = sortedTransactions.foldLeft(Acc.empty) { case (Acc(soldPositions, transactions), transaction) =>
          if (transaction.tpe != Transaction.Type.Sell)
            Acc(soldPositions, transactions :+ transaction)
          else {
            val buyTransactions = findBuyTransactions(transaction, transactions)
            val transactionsWithoutAlreadyBought = excludeTransactionsTakenByCurrentSellTransaction(transactions, buyTransactions)

            if (isTransactionFromThatYear(transaction, year, zone)) {
              // todo check if sum of bought = sold
              val soldPosition = SoldPosition(transaction, buyTransactions)

              Acc(soldPositions :+ soldPosition, transactionsWithoutAlreadyBought)
            } else
              Acc(soldPositions, transactionsWithoutAlreadyBought)
          }
        }

        result.soldPositions.pure[F]
      }

      private def isTransactionFromThatYear(transaction: Transaction, year: Year, zone: ZoneId) =
        transaction.date.atZone(zone).getYear == year.getValue

      private def findBuyTransactions(sellTransaction: Transaction, allPreviousTransactions: List[Transaction]) =
        NonEmptyList.fromListUnsafe { // todo fixme
          allPreviousTransactions
            .filter(_.stock == sellTransaction.stock)
            .filter(_.tpe == Transaction.Type.Buy)
            .foldLeft(List.empty[Transaction]) { case (bought, buyTransaction) =>
              val currentQuantity = bought.map(_.quantity.value).sum
              val quantityFromCurrentTransaction =
                Integer.min(buyTransaction.quantity.value, sellTransaction.quantity.value - currentQuantity)

              if (quantityFromCurrentTransaction > 0) {
                // see https://issuu.com/sii.org.pl/docs/optymalizacja_podatkowa?ff slide 9
                // provision should be taken proportional when dealing with total cost during sell
                val proportionalProvision =
                  (buyTransaction.provision * (quantityFromCurrentTransaction.toDouble / buyTransaction.quantity.value)).rounded
                bought :+ buyTransaction.copy(quantity = Quantity(quantityFromCurrentTransaction), provision = proportionalProvision)
              } else bought
            }
        }

      private def excludeTransactionsTakenByCurrentSellTransaction(
        transactions: List[Transaction],
        buyTransactions: NonEmptyList[Transaction]
      ) =
        transactions.map(transaction =>
          buyTransactions
            .find(_.id == transaction.id)
            .map(t =>
              transaction.copy(
                quantity = transaction.quantity minus t.quantity,
                // see https://issuu.com/sii.org.pl/docs/optymalizacja_podatkowa?ff slide 9
                // provision should be taken proportional when dealing with total cost during sell
                provision = transaction.provision minusUnsafe t.provision
              )
            )
            .getOrElse(transaction)
        )

    }

  case class Acc(soldPositions: List[SoldPosition], transactions: List[Transaction])

  object Acc {
    def empty: Acc = Acc(Nil, Nil)
  }

}
