package matwojcik.stock.prices.domain

import java.time.LocalDate

import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

trait StockPriceRepository[F[_]] {
  def findStockPrice(stock: Stock.Id, date: LocalDate): F[Option[Money]]
}

object StockPriceRepository {
  def apply[F[_]](using ev: StockPriceRepository[F]): StockPriceRepository[F] = ev
}
