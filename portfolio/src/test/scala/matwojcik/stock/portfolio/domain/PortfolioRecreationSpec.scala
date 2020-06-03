package matwojcik.stock.portfolio.domain

import java.time.ZoneId
import java.time.ZonedDateTime

import cats.effect.IO
import cats.scalatest.EitherValues
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Stock
import matwojcik.stock.portfolio.domain.Portfolio.PortfolioRecreationFailure.DuplicateCreationEvent
import matwojcik.stock.portfolio.domain.Portfolio.PortfolioRecreationFailure.EventNotFromPortfolio
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.CurrencyChanged
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.PortfolioCreated
import matwojcik.stock.portfolio.domain.events.PortfolioDomainEvent.TransactionAdded
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

class PortfolioRecreationSpec extends AnyFeatureSpec with Matchers with GivenWhenThen with EitherValues {
  val emptyPortfolio: Portfolio = Portfolio.empty[IO](Currency("PLN")).unsafeRunSync()
  val portfolioId: Portfolio.Id = Portfolio.Id.create[IO].unsafeRunSync()
  val PLN: Currency = Currency("PLN")
  val EUR: Currency = Currency("EUR")
  val portfolioCreated: PortfolioCreated = PortfolioCreated(portfolioId, PLN)
  val someDate: ZonedDateTime = ZonedDateTime.of(2020, 2, 2, 10, 0, 0, 0, ZoneId.of("Z"))

  Feature("Recreation of Portfolio from events") {

    Scenario("Single creation event") {

      Given("Empty events list")
      val events = List.empty[PortfolioDomainEvent]

      When("Creating portfolio from events")
      val result = Portfolio.fromEvents(portfolioCreated, events)

      Then("Failure should be returned")
      result.value shouldBe Portfolio.empty(portfolioId, PLN)
    }

    Scenario("Multiple creation events") {
      Given("Creation event in events list")
      val events = List(portfolioCreated)

      When("Creating portfolio from events")
      val result = Portfolio.fromEvents(portfolioCreated, events)

      Then("Failure should be returned")
      result.leftValue shouldBe (DuplicateCreationEvent(portfolioCreated))
    }

    Scenario("Transaction event belonging to portfolio") {

      Given("Buy transaction from portfolio")
      val transaction = Transaction(Transaction.Id("1"), Stock.Id("MNU"), Transaction.Type.Buy, Quantity(10), someDate)
      val events = List(
        TransactionAdded(portfolioId, transaction)
      )

      When("Creating portfolio from events")
      val result = Portfolio.fromEvents(portfolioCreated, events)

      Then("Portfolio with that transaction should be returned")
      result.value shouldBe Portfolio.empty(portfolioId, PLN).addTransaction(transaction).value
    }

    Scenario("Transaction event not belonging to portfolio") {

      Given("Transaction from other portfolio")
      val transaction = Transaction(Transaction.Id("1"), Stock.Id("MNU"), Transaction.Type.Buy, Quantity(10), someDate)
      val event = TransactionAdded(Portfolio.Id("OTHER"), transaction)
      val events = List(
        event
      )

      When("Creating portfolio from events")
      val result = Portfolio.fromEvents(portfolioCreated, events)

      Then("Failure should be returned")
      result.leftValue shouldBe EventNotFromPortfolio(event)
    }

    Scenario("Currency change belonging to portfolio") {
      Given("Currency change event")
      val events = List(
        CurrencyChanged(portfolioId, EUR)
      )

      When("Creating portfolio from events")
      val result = Portfolio.fromEvents(portfolioCreated, events)

      Then("Portfolio with that currency should be returned")
      result.value shouldBe Portfolio.empty(portfolioId, PLN).changeCurrency(EUR)
    }

    Scenario("Multiple events") {

      Given("Adding, selling and change currency events")
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("MNU"), Transaction.Type.Buy, Quantity(10), someDate)
      val transaction2 = Transaction(Transaction.Id("2"), Stock.Id("MNU"), Transaction.Type.Sell, Quantity(5), someDate)
      val events = List(
        TransactionAdded(portfolioId, transaction1),
        CurrencyChanged(portfolioId, EUR),
        TransactionAdded(portfolioId, transaction2)
      )

      When("Creating portfolio from events")
      val result = Portfolio.fromEvents(portfolioCreated, events)

      Then("Portfolio with those transactions and currency should be returned")
      result.value shouldBe Portfolio
        .empty(portfolioId, PLN)
        .addTransaction(transaction1)
        .flatMap(_.changeCurrency(EUR).addTransaction(transaction2))
        .value
    }
  }
}
