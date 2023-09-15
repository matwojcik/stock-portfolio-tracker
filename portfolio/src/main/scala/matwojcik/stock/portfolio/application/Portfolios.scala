package matwojcik.stock.portfolio.application

import cats.Id
import cats.data.Chain
import cats.data.Writer
import cats.data.WriterT
import cats.effect.Sync
import cats.implicits._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import matwojcik.stock.domain.Currency
import matwojcik.stock.portfolio.domain.Portfolio
import matwojcik.stock.portfolio.domain.PortfolioRepository
import matwojcik.stock.portfolio.domain.Transaction
import matwojcik.stock.portfolio.domain.Portfolio.failures.NotEnoughBalance
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent

trait Portfolios[F[_]] {
  def create(currency: Currency): F[Portfolio.Id]
  def addTransaction(portfolioId: Portfolio.Id, transaction: Transaction): F[Either[NotEnoughBalance, Unit]]
}

object Portfolios {
  def apply[F[_]](implicit ev: Portfolios[F]): Portfolios[F] = ev

  def instance[F[_]: PortfolioRepository: Sync]: Portfolios[F] = new Portfolios[F] {
    private val logger = Slf4jLogger.getLoggerFromClass[F](getClass())

    type EventWriter[A] = Writer[Chain[PortfolioDomainEvent], A]

    def create(currency: Currency): F[Portfolio.Id] =
      for {
        id <- Portfolio.Id.create[F]
        (events, portfolio) = Portfolio.commands.create[EventWriter](id, currency).run
        _ <- logger.info(s"Created portfolio: $portfolio")
        _ <- events.traverse_(PortfolioRepository[F].store(_))
      } yield id

    def addTransaction(portfolioId: Portfolio.Id, transaction: Transaction): F[Either[NotEnoughBalance, Unit]] = ???
  }
}
