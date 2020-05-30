package matwojcik.stock.portfolio.application

import matwojcik.stock.domain.Currency
import matwojcik.stock.portfolio.domain.Portfolio
import matwojcik.stock.portfolio.domain.Portfolio.failures.NotEnoughBalance
import matwojcik.stock.portfolio.domain.Transaction

trait Portfolios[F[_]] {
  def create(currency: Currency): F[Unit]
  def addTransaction(portfolioId: Portfolio.Id, transaction: Transaction): F[Either[NotEnoughBalance, Unit]]
}

object Portfolios {
  def apply[F[_]](implicit ev: Portfolios[F]): Portfolios[F] = ev

  def instance[F[_]]: Portfolios[F] = new Portfolios[F] {
    def create(currency: Currency): F[Unit] = ???
    def addTransaction(portfolioId: Portfolio.Id, transaction: Transaction): F[Either[NotEnoughBalance, Unit]] = ???
  }
}
