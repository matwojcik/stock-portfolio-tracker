package matwojcik.stock.portfolio.domain

import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent

trait PortfolioRepository[F[_]] {
  def find(id: Portfolio.Id): F[Option[Portfolio]]
  def store(event: PortfolioDomainEvent): F[Unit]
}

object PortfolioRepository {
  def apply[F[_]](implicit ev: PortfolioRepository[F]): PortfolioRepository[F] = ev
}
