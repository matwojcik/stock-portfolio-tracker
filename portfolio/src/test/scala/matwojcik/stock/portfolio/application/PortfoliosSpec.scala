package matwojcik.stock.portfolio.application

import java.time.ZoneId
import java.time.ZonedDateTime

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import matwojcik.stock.domain.Currency
import matwojcik.stock.portfolio.domain.Portfolio
import matwojcik.stock.portfolio.domain.PortfolioRepository
import org.scalatest.GivenWhenThen
import org.scalatest.compatible.Assertion
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

class PortfoliosSpec extends AnyFeatureSpec with Matchers with GivenWhenThen with EitherValues {
  val portfolioId: Portfolio.Id = Portfolio.Id("1")
  val PLN: Currency = Currency("PLN")
  val EUR: Currency = Currency("EUR")
  val someDate: ZonedDateTime = ZonedDateTime.of(2020, 2, 2, 10, 0, 0, 0, ZoneId.of("Z"))

  Feature("Creating portfolio") {
    Scenario("Successful creation") {
      withEmptyState { case (repository, portfolios) =>
        for {
          id        <- portfolios.create(PLN)
          portfolio <- repository.find(id)
        } yield portfolio should contain(Portfolio.empty(id, PLN))

      }
    }
  }

  private def withEmptyState(test: (PortfolioRepository[IO], Portfolios[IO]) => IO[Assertion]): Assertion =
    PortfolioRepository.ref[IO]().flatMap(implicit repo => test(repo, Portfolios.instance[IO])).unsafeRunSync()

}
