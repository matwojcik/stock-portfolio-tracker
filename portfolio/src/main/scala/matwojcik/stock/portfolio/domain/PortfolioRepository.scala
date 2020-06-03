package matwojcik.stock.portfolio.domain

import cats.data.Chain
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.mtl.MonadState
import com.olegpy.meow.hierarchy._
import com.olegpy.meow.effects._
import cats.implicits._
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.PortfolioCreated

trait PortfolioRepository[F[_]] {
  def find(id: Portfolio.Id): F[Option[Portfolio]]
  def store(event: PortfolioDomainEvent): F[Unit]
}

object PortfolioRepository {
  def apply[F[_]](implicit ev: PortfolioRepository[F]): PortfolioRepository[F] = ev

  type DomainEvents = Chain[PortfolioDomainEvent]
  type EventLog[F[_]] = MonadState[F, DomainEvents]

  def memory[F[_]: Sync](implicit Events: EventLog[F]): PortfolioRepository[F] =
    new PortfolioRepository[F] {

      override def find(id: Portfolio.Id): F[Option[Portfolio]] =
        Events.get.flatMap { events =>
          events.filter(_.portfolioId == id).toList match {
            case (creation: PortfolioCreated) :: rest =>
              Portfolio
                .fromEvents(creation, rest)
                .fold(
                  failure => Sync[F].raiseError(new IllegalStateException(s"Invalid portfolio state due to: $failure, events: $events")),
                  _.some.pure[F]
                )
            case notCreationEvent :: rest =>
              Sync[F].raiseError(new IllegalStateException(s"Illegal portfolio state, first event is not creation: $events"))
            case Nil =>
              none[Portfolio].pure[F]
          }
        }

      override def store(event: PortfolioDomainEvent): F[Unit] =
        Events.modify(_ :+ event)
    }

  def ref[F[_]: Sync](initial: DomainEvents = Chain.empty): F[PortfolioRepository[F]] =
    for {
      ref  <- Ref.of[F, DomainEvents](initial)
      repo <- ref.runState(implicit ft => Sync[F].delay(memory[F]))
    } yield repo
}
