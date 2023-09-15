package matwojcik.stock.portfolio.domain

import java.util.UUID

import cats.data.Chain
import cats.Functor
import cats.effect.Sync
import cats.implicits._
import cats.mtl.Tell
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
      case Type.Buy  =>
        addStock(stock, quantity).asRight[NotEnoughBalance]
      case Type.Sell =>
        holdings.get(stock) match {
          case Some(holding) =>
            Either.cond(holding.balance >= quantity, holding.copy(balance = holding.balance minus quantity), NotEnoughBalance(stock))
          case None          =>
            NotEnoughBalance(stock).asLeft[Holding]
        }
    }

    newHolding.map(holding =>
      Portfolio(
        id = id,
        currency = currency,
        holdings = (holdings + (stock -> holding)).filter { case (_, holding) =>
          holding.balance.value > 0
        }
      )
    )
  }

  private def addStock(stock: Stock.Id, quantity: Quantity) =
    holdings.get(stock).fold(Holding(stock, quantity))(holding => holding.copy(balance = holding.balance plus quantity))

  def changeCurrency(newCurrency: Currency): Portfolio =
    Portfolio(id, newCurrency, holdings)

  def activeHoldings: List[Holding] = holdings.values.toList

}

object Portfolio {
  def empty[F[_]: Sync](currency: Currency): F[Portfolio] = Id.create.map(Portfolio.empty(_, currency))
  def empty(id: Portfolio.Id, currency: Currency): Portfolio = Portfolio(id, currency, Map.empty)

  def fromEvents(creation: PortfolioCreated, events: List[PortfolioDomainEvent]): Either[PortfolioRecreationFailure, Portfolio] =
    events.foldM[({ type Λ$[β$0$] = Either[PortfolioRecreationFailure, β$0$] })#Λ$, Portfolio](
      Portfolio.empty(creation.portfolioId, creation.currency)
    ) { case (portfolio, event) =>
      event match {
        case e: PortfolioCreated                                        =>
          DuplicateCreationEvent(e).asLeft[Portfolio]
        case e @ PortfolioDomainEvent.TransactionAdded(id, transaction) =>
          if (id == portfolio.id)
            portfolio.addTransaction(transaction).leftMap(DomainFailure(_))
          else
            EventNotFromPortfolio(e).asLeft[Portfolio]
        case e @ CurrencyChanged(portfolioId, currency)                 =>
          if (portfolioId == portfolio.id)
            portfolio.changeCurrency(currency).asRight[PortfolioRecreationFailure]
          else
            EventNotFromPortfolio(e).asLeft[Portfolio]
      }
    }

  object commands {

    type EventLog[F[_]] = Tell[F, Chain[PortfolioDomainEvent]]

    def create[F[_]: Functor](id: Portfolio.Id, currency: Currency)(using Events: EventLog[F]): F[Portfolio] =
      Events.tell(Chain.one(PortfolioCreated(id, currency))).as(Portfolio.empty(id, currency))

    def changeCurrency[F[_]: Functor](portfolio: Portfolio)(newCurrency: Currency)(using Events: EventLog[F]): F[Portfolio] =
      Events.tell(Chain.one(CurrencyChanged(portfolio.id, newCurrency))).as(portfolio.changeCurrency(newCurrency))

    def addTransaction[F[_]: Functor](
      portfolio: Portfolio
    )(
      transaction: Transaction
    )(
      implicit Events: EventLog[F]
    ): Either[NotEnoughBalance, F[Portfolio]] =
      portfolio.addTransaction(transaction).map(p => Events.tell(Chain.one(TransactionAdded(portfolio.id, transaction))).as(p))

  }

  enum PortfolioRecreationFailure:
    case DuplicateCreationEvent(event: PortfolioCreated)
    case EventNotFromPortfolio(event: PortfolioDomainEvent)
    case DomainFailure(reason: NotEnoughBalance)

  case class Id(value: String)

  object Id {
    def create[F[_]: Sync]: F[Id] = Sync[F].delay(Id(UUID.randomUUID().toString))
  }

  object failures {
    case class NotEnoughBalance(stock: Stock.Id)
  }

}

case class Holding(stock: Stock.Id, balance: Quantity)
