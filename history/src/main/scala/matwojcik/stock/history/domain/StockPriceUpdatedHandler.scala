package matwojcik.stock.history.domain

import java.time.LocalDate

import cats.Monad
import matwojcik.cats.syntax.eithert._
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock
import matwojcik.stock.history.domain.StockPriceUpdatedHandler.failures.StockPriceUpdateFailure
import matwojcik.stock.history.domain.StockPriceUpdatedHandler.failures.StockPriceUpdateFailure.NotFound

trait StockPriceUpdatedHandler[F[_]] {
  def handleStockPriceUpdated(event: StockPriceUpdated): F[Either[StockPriceUpdateFailure, Unit]]
}

object StockPriceUpdatedHandler {
  def apply[F[_]](implicit ev: StockPriceUpdatedHandler[F]): StockPriceUpdatedHandler[F] = ev

  def instance[F[_]: Currencies: HoldingRepository: PortfolioRepository: HoldingHistoryRepository: Monad]: StockPriceUpdatedHandler[F] =
    new StockPriceUpdatedHandler[F] {

      override def handleStockPriceUpdated(event: StockPriceUpdated): F[Either[StockPriceUpdateFailure, Unit]] = {
        for {
          holding <- HoldingRepository[F].findHoldingAt(event.portfolio, event.stock, event.date).toEitherT(NotFound(event.portfolio))
          portfolioCurrency <- PortfolioRepository[F].findCurrencyOfPortfolio(event.portfolio).toEitherT(NotFound(event.portfolio))
          currencyRate      <- Currencies[F]
                                 .findCurrencyRate(event.price.currency, portfolioCurrency, event.date)
                                 .liftToEitherT[StockPriceUpdateFailure]
          _                 <- storeHoldingHistory(event, holding, portfolioCurrency, currencyRate)
        } yield ()
      }.value

      private def storeHoldingHistory(
        event: StockPriceUpdated,
        holding: Holding,
        portfolioCurrency: Currency,
        currencyRate: CurrencyRate
      ) = {
        val totalValue = event.price * holding.quantity
        val history = HoldingHistory(
          event.portfolio,
          event.stock,
          event.date,
          holding.quantity,
          event.price,
          totalValue,
          totalValue.to(portfolioCurrency)(currencyRate)
        )
        HoldingHistoryRepository[F].store(history).liftToEitherT[StockPriceUpdateFailure]
      }

    }

  object failures {
    sealed trait StockPriceUpdateFailure extends Product with Serializable

    object StockPriceUpdateFailure {
      case class NotFound(id: Portfolio.Id) extends StockPriceUpdateFailure
    }

  }

}

case class StockPriceUpdated(portfolio: Portfolio.Id, stock: Stock.Id, price: Money, date: LocalDate)
