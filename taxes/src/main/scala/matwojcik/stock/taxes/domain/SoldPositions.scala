package matwojcik.stock.taxes.domain

import cats.Applicative
import cats.syntax.all._
import cats.data.NonEmptyList
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.taxes.domain.Income.SoldPosition

import java.time.Year
import java.time.ZoneId

trait SoldPositions[F[_]] {
  def findSoldPositions(year: Year, zone: ZoneId, transactions: NonEmptyList[Transaction]): F[List[SoldPosition]]
}

object SoldPositions {
  def apply[F[_]](implicit ev: SoldPositions[F]): SoldPositions[F] = ev

  def instance[F[_]: Applicative]: SoldPositions[F] =
    new SoldPositions[F] {

      override def findSoldPositions(year: Year, zone: ZoneId, transactions: NonEmptyList[Transaction]): F[List[SoldPosition]] = {

        val sortedTransactions = transactions.sortBy(_.date.toEpochMilli)

        val result = sortedTransactions.foldLeft(Acc.empty) {
          case (Acc(soldPositions, transactions), transaction) =>
            if (transaction.date.atZone(zone).getYear != year.getValue || transaction.tpe != Transaction.Type.Sell)
              Acc(soldPositions, transactions :+ transaction)
            else {
              val buyTransactions = NonEmptyList.fromListUnsafe { // todo fixme
                transactions
                  .filter(_.stock == transaction.stock)
                  .filter(_.tpe == Transaction.Type.Buy)
                  .foldLeft(List.empty[Transaction]) {
                    case (bought, buyTransaction) =>
                      val currentQuantity = bought.map(_.quantity.value).fold(0)(_ + _)
                      val quantityFromCurrentTransaction = Integer.min(buyTransaction.quantity.value, transaction.quantity.value - currentQuantity)
                      if (quantityFromCurrentTransaction > 0)
                        bought :+ buyTransaction.copy(quantity = Quantity(quantityFromCurrentTransaction))
                      else bought
                  }
              }
              // todo check if sum of bought = sold
              val soldPosition = SoldPosition(transaction, buyTransactions)
              Acc(soldPositions :+ soldPosition, transactions)
            }
        }

        result.soldPositions.pure[F]
      }

    }

  case class Acc(soldPositions: List[SoldPosition], transactions: List[Transaction])

  object Acc {
    def empty: Acc = Acc(Nil, Nil)
  }

}
