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

      override def calculate(year: Year, zone: ZoneId, currency: Currency, transactions: NonEmptyList[Transaction]): F[List[Income]] =
        for {
          soldPositions <- SoldPositions[F].findSoldPositions(year, zone, transactions)
          soldPositionsIncome = calculateTotalCostOfSoldPositions(soldPositions, currency)
          provisions = findProvisions(year, zone, currency, transactions)
        } yield provisions ++ soldPositionsIncome

      private def findProvisions(year: Year, zone: ZoneId, currency: Currency, transactions: NonEmptyList[Transaction]) =
        transactions
          .filter(_.date.atZone(zone).getYear == year.getValue)
          .filterNot(_.provision.value == 0)
          .map(transaction =>
            Provision(
              transaction.date,
              transaction.provisionInAccountingCurrency(currency) * Quantity(-1),
              transaction
            )
          )

      private def calculateTotalCostOfSoldPositions(soldPositions: List[Income.SoldPosition], currency: Currency) =
        soldPositions.map { soldPosition =>
          Income.StockSell(soldPosition.sellTransaction.date, soldPosition.income(currency), soldPosition)
        }

    }

}
