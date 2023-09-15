package matwojcik.stock.prices.domain

import cats.data.NonEmptySet
import fs2.Stream
import matwojcik.stock.domain.Stock

trait PortfolioStockRepository[F[_]] {
  def findStocksInAllPortfolios(): Stream[F, StockPortfolios]
}

object PortfolioStockRepository {
  def apply[F[_]](implicit ev: PortfolioStockRepository[F]): PortfolioStockRepository[F] = ev
}

case class StockPortfolios(stock: Stock.Id, portfolios: NonEmptySet[Portfolio.Id])

object Portfolio {
  case class Id(value: String)
}
