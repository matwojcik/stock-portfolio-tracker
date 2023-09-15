package matwojcik.stock.prices.domain

import matwojcik.stock.domain.Stock

trait ObtainStockPriceHandler[F[_]] {
  def handle(command: ObtainStockPriceCommand): F[Unit]
}

object ObtainStockPriceHandler {
  def apply[F[_]](using ev: ObtainStockPriceHandler[F]): ObtainStockPriceHandler[F] = ev
}

enum ObtainStockPriceCommand:
  case ObtainAllStockPrices
  case ObtainStockFor(id: Stock.Id, protfolios: Portfolio.Id)
