package matwojcik.stock.history.domain

import java.time.LocalDate

import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity

trait HoldingRepository[F[_]] {

  // Maybe Holding should be the aggregate instead?
  def findHoldingAt(id: Portfolio.Id, stock: Stock.Id, date: LocalDate): F[Option[Holding]]
}

object HoldingRepository {
  def apply[F[_]](implicit ev: HoldingRepository[F]): HoldingRepository[F] = ev

}

case class Holding(id: Portfolio.Id, stock: Stock.Id, quantity: Quantity)
