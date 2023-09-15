package matwojcik.stock.portfolio.domain

import matwojcik.stock.domain.Currency

object events {

  enum PortfolioDomainEvent(val portfolioId: Portfolio.Id):
    case PortfolioCreated(id: Portfolio.Id, currency: Currency) extends PortfolioDomainEvent(id)
    case TransactionAdded(id: Portfolio.Id, transaction: Transaction) extends PortfolioDomainEvent(id)
    case CurrencyChanged(id: Portfolio.Id, currency: Currency) extends PortfolioDomainEvent(id)

}
