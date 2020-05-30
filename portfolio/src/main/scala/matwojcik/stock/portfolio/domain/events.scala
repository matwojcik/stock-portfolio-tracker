package matwojcik.stock.portfolio.domain

import matwojcik.stock.domain.Currency

object events {

  sealed trait PortfolioDomainEvent extends Product with Serializable

  object PortfolioDomainEvent {

    case class PortfolioCreated(id: Portfolio.Id, currency: Currency) extends PortfolioDomainEvent
    case class TransactionAdded(portfolio: Portfolio.Id, transaction: Transaction) extends PortfolioDomainEvent
    case class CurrencyChanged(portfolio: Portfolio.Id, currency: Currency) extends PortfolioDomainEvent

  }

}
