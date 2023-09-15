package matwojcik.stock.portfolio.domain

import java.time.ZoneId
import java.time.ZonedDateTime

import cats.effect.IO
import org.scalatest.EitherValues
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.CurrencyChanged
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.PortfolioCreated
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.TransactionAdded
import org.scalatest.GivenWhenThen
import org.scalatest.compatible.Assertion
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global

class PortfolioRepositorySpec extends AnyFeatureSpec with Matchers with GivenWhenThen with EitherValues {
  val portfolioId: Portfolio.Id = Portfolio.Id("1")
  val PLN: Currency = Currency("PLN")
  val EUR: Currency = Currency("EUR")
  val someDate: ZonedDateTime = ZonedDateTime.of(2020, 2, 2, 10, 0, 0, 0, ZoneId.of("Z"))

  Feature("Finding the portfolio") {
    Scenario("empty repo") {
      withEmptyRepo { repository =>
        Given("Empty repository")

        for {
          portfolio <- repository.find(portfolioId)
        } yield portfolio shouldBe (empty)
      }
    }

    Scenario("not existing portfolio") {
      withEmptyRepo { repository =>
        Given("Repository with some other portfolio")

        for {
          _         <- repository.store(PortfolioCreated(Portfolio.Id("OTHER"), PLN))
          portfolio <- repository.find(portfolioId)
        } yield portfolio shouldBe (empty)
      }
    }

    Scenario("existing portfolio") {
      withEmptyRepo { repository =>
        Given("Empty repository")

        for {
          _         <- repository.store(PortfolioCreated(portfolioId, PLN))
          portfolio <- repository.find(portfolioId)
        } yield portfolio should contain(Portfolio.empty(portfolioId, PLN))
      }
    }

    Scenario("portfolio with transactions") {
      withEmptyRepo { repository =>
        Given("Empty repository")

        for {
          _ <- repository.store(PortfolioCreated(portfolioId, PLN))
          transaction1 = Transaction(Transaction.Id("1"), Stock.Id("MNU"), Transaction.Type.Buy, Quantity(10), someDate)
          transaction2 = Transaction(Transaction.Id("2"), Stock.Id("MNU"), Transaction.Type.Sell, Quantity(3), someDate)
          _         <- repository.store(TransactionAdded(portfolioId, transaction1))
          _         <- repository.store(CurrencyChanged(portfolioId, EUR))
          _         <- repository.store(TransactionAdded(portfolioId, transaction2))
          portfolio <- repository.find(portfolioId)
        } yield portfolio should contain(
          Portfolio.empty(portfolioId, PLN).addTransaction(transaction1).flatMap(_.changeCurrency(EUR).addTransaction(transaction2)).value
        )
      }
    }

    Scenario("Incorrect transactions") {
      withEmptyRepo { repository =>
        val result = for {
          _ <- repository.store(PortfolioCreated(portfolioId, PLN))
          transaction = Transaction(Transaction.Id("2"), Stock.Id("MNU"), Transaction.Type.Sell, Quantity(3), someDate)
          _         <- repository.store(TransactionAdded(portfolioId, transaction))
          portfolio <- repository.find(portfolioId)
        } yield portfolio

        IO(assertThrows[IllegalStateException](result.unsafeRunSync()))
      }
    }
  }

  private def withEmptyRepo(test: PortfolioRepository[IO] => IO[Assertion]): Assertion =
    PortfolioRepository.ref[IO]().flatMap(test).unsafeRunSync()

}
