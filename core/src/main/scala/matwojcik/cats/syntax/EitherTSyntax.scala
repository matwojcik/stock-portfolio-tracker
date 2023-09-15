package matwojcik.cats.syntax

import cats.Functor
import cats.data.EitherT

trait EitherTSyntax {

  extension[F[_], A](fa: F[A]) {
    def liftToEitherT[E](using F: Functor[F]): EitherT[F, E, A] = EitherT.right[E](fa)
  }
  
  extension[F[_], A](foa: F[Option[A]]) {
    def toEitherT[E](ifNone: => E)(using F: Functor[F]): EitherT[F, E, A] = EitherT.fromOptionF(foa, ifNone)
  }

}

object eithert extends EitherTSyntax
