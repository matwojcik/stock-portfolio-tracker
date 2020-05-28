package matwojcik.stock.portfolio.domain

import java.time.ZoneId
import java.time.ZonedDateTime

import cats.scalatest.EitherValues
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.portfolio.domain.Portfolio.failures.NotEnoughBalance
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.GivenWhenThen

class PortfolioSpec extends AnyFeatureSpec with Matchers with GivenWhenThen with EitherValues {

  val someDate: ZonedDateTime = ZonedDateTime.of(2020, 2, 2, 10, 0, 0, 0, ZoneId.of("Z"))

  Feature("Adding buy transaction") {
    Scenario("Empty portfolio") {
      val portfolio = Portfolio.empty

      When("New buy transaction is added")
      val transaction = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(10), someDate)
      val newPortfolio = portfolio.addTransaction(transaction)

      Then("Portfolio should have single holding with that stock")
      newPortfolio.value.activeHoldings should contain only (Holding(transaction.stock, transaction.quantity))
    }

    Scenario("Existing holding") {
      When("Portfolio contains holding for stock")
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(10), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New buy transaction of the same stock is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val newPortfolio = portfolio.value.addTransaction(transaction2)

      Then("Portfolio should have single holding with that stock with increased quantity")
      newPortfolio.value.activeHoldings should contain only (Holding(
        transaction1.stock,
        transaction1.quantity plus transaction2.quantity
      ))
    }

    Scenario("New holding") {
      When("Portfolio has some holding")
      val transaction1 = Transaction(Transaction.Id("2"), Stock.Id("CDP"), Transaction.Type.Buy, Stock.Quantity(10), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New buy transaction of different stock is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val newPortfolio = portfolio.value.addTransaction(transaction2)

      Then("Portfolio should have two holdings")
      newPortfolio.value.activeHoldings should contain only (Holding(transaction1.stock, transaction1.quantity), Holding(
        transaction2.stock,
        transaction2.quantity
      ))
    }
  }

  Feature("Adding sell transaction") {
    Scenario("No such stock") {
      Given("Portfolio is empty")
      val portfolio = Portfolio.empty

      When("New sell transaction is added")
      val transaction = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(10), someDate)
      val result = portfolio.addTransaction(transaction)

      Then("Portfolio should not be changed returning failure instead")
      result.leftValue shouldBe (NotEnoughBalance(transaction.stock))
    }

    Scenario("Not enough stock") {
      When("Portfolio has some holding")
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New sell transaction is added which exceeds holding")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(10), someDate)
      val result = portfolio.value.addTransaction(transaction2)

      Then("Portfolio should not be changed returning failure instead")
      result.leftValue shouldBe (NotEnoughBalance(transaction2.stock))
    }

    Scenario("Just enough stock") {
      When("Portfolio has some holding")
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New sell transaction is added which equals holding")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(5), someDate)
      val result = portfolio.value.addTransaction(transaction2)

      Then("Holding should be removed from portfolio")
      result.value.activeHoldings shouldBe empty
    }

    Scenario("More than enough stock") {
      When("Portfolio has some holding")
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(6), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New sell transaction is added which is less than holding")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(5), someDate)
      val result = portfolio.value.addTransaction(transaction2)

      Then("Holding should be adjusted")
      result.value.activeHoldings should contain only Holding(transaction1.stock, Quantity(1))
    }
  }

}
