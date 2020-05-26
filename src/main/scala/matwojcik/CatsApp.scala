package matwojcik

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Sync
import cats.syntax.all._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

object CatsApp extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    program[IO].as(ExitCode.Success)

  def program[F[_]: Sync] = {
    val logger = Slf4jLogger.getLogger[F]

    logger.info("Starting") *> 0.pure[F]
  }
}
