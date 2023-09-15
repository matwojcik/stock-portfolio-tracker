package matwojcik.stock.history.domain

import matwojcik.stock.domain.Stock

trait HoldingHistoryRepository[F[_]] {
  def store(holdingHistory: HoldingHistory): F[Unit]
  def find(portfolio: Portfolio.Id, stock: Stock.Id): F[Option[HoldingHistory]]
}

object HoldingHistoryRepository {
  def apply[F[_]](using ev: HoldingHistoryRepository[F]): HoldingHistoryRepository[F] = ev
}
