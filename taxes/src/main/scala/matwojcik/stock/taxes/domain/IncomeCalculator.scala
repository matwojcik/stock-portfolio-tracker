package matwojcik.stock.taxes.domain

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.all._
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.taxes.domain.Income.Provision

import java.time.Year
import java.time.ZoneId

trait IncomeCalculator[F[_]] {
  def calculate(year: Year, zone: ZoneId, currency: Currency, transactions: NonEmptyList[Transaction]): F[List[Income]]
}

object IncomeCalculator {
  def apply[F[_]](implicit ev: IncomeCalculator[F]): IncomeCalculator[F] = ev

  def instance[F[_]: SoldPositions: Monad]: IncomeCalculator[F] =
    new IncomeCalculator[F] {

      override def calculate(year: Year, zone: ZoneId, currency: Currency, transactions: NonEmptyList[Transaction]): F[List[Income]] = {

        def findProvisions(transactions: NonEmptyList[Transaction]) =
          transactions
            .filter(_.date.atZone(zone).getYear == year.getValue)
            .map(transaction =>
              Provision(transaction.date, transaction.cost.to(currency)(transaction.costExchangeRate) * Quantity(-1), transaction)
            )

        for {
          soldPositions <- SoldPositions[F].findSoldPositions(year, zone, transactions)
          soldPositionsIncome = calculateTotalCostOfSoldPositions(soldPositions, currency)
          provisions = findProvisions(transactions)
        } yield provisions ++ soldPositionsIncome
      }

      private def calculateTotalCostOfSoldPositions(soldPositions: List[Income.SoldPosition], currency: Currency) =
        soldPositions.map { soldPosition =>
          Income.StockSell(soldPosition.sellTransaction.date, soldPosition.income(currency), soldPosition)
        }


    }

}
