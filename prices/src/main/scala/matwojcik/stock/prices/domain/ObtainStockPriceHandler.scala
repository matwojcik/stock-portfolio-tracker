package matwojcik.stock.prices.domain

import matwojcik.stock.domain.Stock

trait ObtainStockPriceHandler[F[_]] {
  def handle(command: ObtainStockPriceCommand): F[Unit]
}

object ObtainStockPriceHandler {
  def apply[F[_]](implicit ev: ObtainStockPriceHandler[F]): ObtainStockPriceHandler[F] = ev
}

sealed trait ObtainStockPriceCommand extends Product with Serializable

object ObtainStockPriceCommand {
  case object ObtainAllStockPrices extends ObtainStockPriceCommand
  case class ObtainStockFor(id: Stock.Id, protfolios: Portfolio.Id) extends ObtainStockPriceCommand
}
