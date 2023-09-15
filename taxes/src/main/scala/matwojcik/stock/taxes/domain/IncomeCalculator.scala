package matwojcik.stock.taxes.domain

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.all._
import matwojcik.stock.domain.Currency

import java.time.Year
import java.time.ZoneId

trait IncomeCalculator[F[_]] {
  def calculate(year: Year, zone: ZoneId, currency: Currency, transactions: NonEmptyList[Transaction]): F[List[Income]]
}

object IncomeCalculator {
  def apply[F[_]](using ev: IncomeCalculator[F]): IncomeCalculator[F] = ev

  def instance[F[_]: SoldPositions: Monad]: IncomeCalculator[F] =
    new IncomeCalculator[F] {

      override def calculate(year: Year, zone: ZoneId, currency: Currency, transactions: NonEmptyList[Transaction]): F[List[Income]] =
        for {
          soldPositions <- SoldPositions[F].findSoldPositions(year, zone, transactions)
          soldPositionsIncome = calculateTotalCostOfSoldPositions(soldPositions, currency)
        } yield soldPositionsIncome

      private def calculateTotalCostOfSoldPositions(soldPositions: List[Income.SoldPosition], currency: Currency) =
        soldPositions.map { soldPosition =>
          Income(soldPosition.sellTransaction.date, soldPosition.netIncome(currency), soldPosition)
        }

    }

}
