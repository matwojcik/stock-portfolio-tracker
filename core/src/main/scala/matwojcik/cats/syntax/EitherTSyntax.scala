package matwojcik.cats.syntax

import cats.Functor
import cats.data.EitherT

trait EitherTSyntax {

  implicit final class FEitherToEitherT[F[_], E, A](private val fea: F[Either[E, A]]) {
    def toEitherT: EitherT[F, E, A] = EitherT(fea)
  }

  implicit final class FToEitherT[F[_], A](private val fa: F[A]) {
    def liftToEitherT[E](implicit F: Functor[F]): EitherT[F, E, A] = EitherT.right[E](fa)
  }

  implicit final class FOptionToEitherT[F[_], A](private val foa: F[Option[A]]) {
    def toEitherT[E](ifNone: => E)(implicit F: Functor[F]): EitherT[F, E, A] = EitherT.fromOptionF(foa, ifNone)
  }
}

object eithert extends EitherTSyntax
