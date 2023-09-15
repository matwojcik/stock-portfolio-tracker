package matwojcik.stock.portfolio.domain

import cats.data.Chain
import cats.effect.Sync
import cats.effect.kernel.Ref
import cats.mtl.Stateful
import cats.implicits._
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.PortfolioCreated
import cats.Functor
import cats.mtl.Tell
import cats.kernel.Semigroup
import cats.Monad

trait PortfolioRepository[F[_]] {
  def find(id: Portfolio.Id): F[Option[Portfolio]]
  def store(event: PortfolioDomainEvent): F[Unit]
}

object PortfolioRepository {
  def apply[F[_]](using ev: PortfolioRepository[F]): PortfolioRepository[F] = ev

  type DomainEvents = Chain[PortfolioDomainEvent]
  type EventLog[F[_]] = Stateful[F, DomainEvents]

  def memory[F[_]: Sync](using Events: EventLog[F]): PortfolioRepository[F] =
    new PortfolioRepository[F] {

      override def find(id: Portfolio.Id): F[Option[Portfolio]] =
        Events.get.flatMap { events =>
          events.filter(_.portfolioId == id).toList match {
            case (creation: PortfolioCreated) :: rest =>
              Portfolio
                .fromEvents(creation, rest)
                .fold(
                  failure => Sync[F].raiseError(new IllegalStateException(s"Invalid portfolio state due to: $failure, events: $events")),
                  portfolio => portfolio.some.pure[F]
                )
            case notCreationEvent :: rest             =>
              Sync[F].raiseError(new IllegalStateException(s"Illegal portfolio state, first event is not creation: $events"))
            case Nil                                  =>
              none[Portfolio].pure[F]
          }
        }

      override def store(event: PortfolioDomainEvent): F[Unit] =
        Events.modify(_ :+ event)
    }

  class RefStateful[F[_]: Monad, S](ref: Ref[F, S]) extends Stateful[F, S] {
    val monad: Monad[F] = summon
    def get: F[S] = ref.get
    def set(s: S): F[Unit] = ref.set(s)
    override def inspect[A](f: S => A): F[A] = ref.get.map(f)
    override def modify(f: S => S): F[Unit] = ref.update(f)
  }

  def ref[F[_]: Sync](initial: DomainEvents = Chain.empty): F[PortfolioRepository[F]] =
    for {
      ref  <- Ref.of[F, DomainEvents](initial)
      repo <- {
        given r: RefStateful[F, DomainEvents] = new RefStateful(ref)
        Sync[F].delay(memory[F])
      }
    } yield repo

}
