package matwojcik.stock.portfolio.domain

object events {
  sealed trait PortfolioDomainEvent extends Product with Serializable

  object PortfolioDomainEvent {
    case class TransactionAdded(portfolio: Portfolio.Id, transaction: Transaction) extends PortfolioDomainEvent
  }
}
