package matwojcik.stock.portfolio.domain

import java.util.UUID

import cats.data.Chain
import cats.effect.Sync
import cats.implicits._
import com.olegpy.meow.prelude._
import cats.mtl.FunctorTell
import io.estatico.newtype.macros.newtype
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.portfolio.domain.Portfolio.PortfolioRecreationFailure.DomainFailure
import matwojcik.stock.portfolio.domain.Portfolio.PortfolioRecreationFailure.DuplicateCreationEvent
import matwojcik.stock.portfolio.domain.Portfolio.PortfolioRecreationFailure.EventNotFromPortfolio
import matwojcik.stock.portfolio.domain.Portfolio.failures.NotEnoughBalance
import matwojcik.stock.portfolio.domain.Transaction.Type
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.CurrencyChanged
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.PortfolioCreated
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.TransactionAdded

case class Portfolio private (id: Portfolio.Id, currency: Currency, private val holdings: Map[Stock.Id, Holding]) {

  def addTransaction(transaction: Transaction): Either[NotEnoughBalance, Portfolio] = {
    val stock = transaction.stock
    val quantity = transaction.quantity
    val newHolding = transaction.tpe match {
      case Type.Buy =>
        addStock(stock, quantity).asRight[NotEnoughBalance]
      case Type.Sell =>
        holdings.get(stock) match {
          case Some(holding) =>
            Either.cond(holding.balance >= quantity, holding.copy(balance = holding.balance minus quantity), NotEnoughBalance(stock))
          case None =>
            NotEnoughBalance(stock).asLeft[Holding]
        }
    }

    newHolding.map(holding =>
      Portfolio(id = id, currency = currency, holdings = (holdings + (stock -> holding)).filter {
        case (_, holding) => holding.balance.value > 0
      })
    )
  }

  private def addStock(stock: Stock.Id, quantity: Quantity) =
    holdings.get(stock).fold(Holding(stock, quantity))(holding => holding.copy(balance = holding.balance plus quantity))

  def changeCurrency(newCurrency: Currency): Portfolio =
    Portfolio(id, newCurrency, holdings)

  def activeHoldings: List[Holding] = holdings.values.toList

  private def copy(): Unit = ()
}

object Portfolio {
  def empty[F[_]: Sync](currency: Currency): F[Portfolio] = Id.create.map(Portfolio.empty(_, currency))
  def empty(id: Portfolio.Id, currency: Currency): Portfolio = Portfolio(id, currency, Map.empty)

  def fromEvents(creation: PortfolioCreated, events: List[PortfolioDomainEvent]): Either[PortfolioRecreationFailure, Portfolio] =
    events.foldM[Either[PortfolioRecreationFailure, *], Portfolio](Portfolio.empty(creation.portfolioId, creation.currency)) {
      case (portfolio, event) =>
        event match {
          case e: PortfolioCreated =>
            DuplicateCreationEvent(e).asLeft[Portfolio]
          case e @ PortfolioDomainEvent.TransactionAdded(id, transaction) =>
            if (id == portfolio.id)
              portfolio.addTransaction(transaction).leftMap(DomainFailure)
            else
              EventNotFromPortfolio(e).asLeft[Portfolio]
          case e @ CurrencyChanged(portfolioId, currency) =>
            if (portfolioId == portfolio.id)
              portfolio.changeCurrency(currency).asRight[PortfolioRecreationFailure]
            else
              EventNotFromPortfolio(e).asLeft[Portfolio]
        }
    }

  object commands {

    type EventLog[F[_]] = FunctorTell[F, Chain[PortfolioDomainEvent]]

    def create[F[_]](id: Portfolio.Id, currency: Currency)(implicit Events: EventLog[F]): F[Portfolio] =
      Events.tell(Chain.one(PortfolioCreated(id, currency))).as(Portfolio.empty(id, currency))

    def changeCurrency[F[_]](portfolio: Portfolio)(newCurrency: Currency)(implicit Events: EventLog[F]): F[Portfolio] =
      Events.tell(Chain.one(CurrencyChanged(portfolio.id, newCurrency))).as(portfolio.changeCurrency(newCurrency))

    def addTransaction[F[_]](
      portfolio: Portfolio
    )(
      transaction: Transaction
    )(
      implicit Events: EventLog[F]
    ): Either[NotEnoughBalance, F[Portfolio]] =
      portfolio.addTransaction(transaction).map(p => Events.tell(Chain.one(TransactionAdded(portfolio.id, transaction))).as(p))

  }

  sealed trait PortfolioRecreationFailure extends Product with Serializable

  object PortfolioRecreationFailure {
    case class DuplicateCreationEvent(event: PortfolioCreated) extends PortfolioRecreationFailure
    case class EventNotFromPortfolio(event: PortfolioDomainEvent) extends PortfolioRecreationFailure
    case class DomainFailure(reason: NotEnoughBalance) extends PortfolioRecreationFailure
  }

  @newtype case class Id(value: String)

  object Id {
    def create[F[_]: Sync]: F[Id] = Sync[F].delay(Id(UUID.randomUUID().toString))
  }

  object failures {
    case class NotEnoughBalance(stock: Stock.Id)
  }
}

case class Holding(stock: Stock.Id, balance: Quantity)
