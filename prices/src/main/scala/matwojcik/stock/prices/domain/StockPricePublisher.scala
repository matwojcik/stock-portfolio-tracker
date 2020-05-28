package matwojcik.stock.prices.domain

import java.time.LocalDate

import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

trait StockPricePublisher[F[_]] {
  def publishPrice(stock: Stock.Id, price: Money, portfolio: Portfolio.Id, date: LocalDate): F[Unit]
}

object StockPricePublisher {
  def apply[F[_]](implicit ev: StockPricePublisher[F]): StockPricePublisher[F] = ev
}
