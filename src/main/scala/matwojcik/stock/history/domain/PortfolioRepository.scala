package matwojcik.stock.history.domain

import matwojcik.stock.domain.Currency

trait PortfolioRepository[F[_]] {
  def findCurrencyOfPortfolio(portfolioId: Portfolio.Id): F[Option[Currency]]
}

object PortfolioRepository {
  def apply[F[_]](implicit ev: PortfolioRepository[F]): PortfolioRepository[F] = ev
}
