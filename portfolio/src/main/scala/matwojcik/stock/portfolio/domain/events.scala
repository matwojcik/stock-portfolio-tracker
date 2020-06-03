package matwojcik.stock.portfolio.domain

import matwojcik.stock.domain.Currency

object events {

  sealed trait PortfolioDomainEvent extends Product with Serializable {
    def portfolioId: Portfolio.Id
  }

  object PortfolioDomainEvent {
    // TODO make them private somehow
    case class PortfolioCreated(portfolioId: Portfolio.Id, currency: Currency) extends PortfolioDomainEvent
    case class TransactionAdded(portfolioId: Portfolio.Id, transaction: Transaction) extends PortfolioDomainEvent
    case class CurrencyChanged(portfolioId: Portfolio.Id, currency: Currency) extends PortfolioDomainEvent
  }

}
