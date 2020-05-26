package matwojcik.stock.portfolio

import java.time.ZoneId
import java.time.ZonedDateTime

import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.portfolio.Portfolio.failures.NotEnoughBalance
import org.scalatest.EitherValues
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

class PortfolioSpec extends AnyFeatureSpec with Matchers with GivenWhenThen with EitherValues {

  val someDate: ZonedDateTime = ZonedDateTime.of(2020, 2, 2, 10, 0, 0, 0, ZoneId.of("Z"))

  Feature("Adding buy transaction") {
    Scenario("Empty portfolio") {
      val portfolio = Portfolio.empty

      When("New transaction is added")
      val transaction = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(10), someDate)
      val newPortfolio = portfolio.addTransaction(transaction)

      Then("Portfolio should have single holding with that stock")
      newPortfolio.right.value.activeHoldings should contain only (Holding(transaction.stock, transaction.quantity))
    }

    Scenario("Portfolio having this stock") {
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(10), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New transaction of the same stock is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val newPortfolio = portfolio.right.value.addTransaction(transaction2)

      Then("Portfolio should have single holding with that stock")
      newPortfolio.right.value.activeHoldings should contain only (Holding(
        transaction1.stock,
        transaction1.quantity plus transaction2.quantity
      ))
    }

    Scenario("Portfolio having other stock") {
      val transaction1 = Transaction(Transaction.Id("2"), Stock.Id("CDP"), Transaction.Type.Buy, Stock.Quantity(10), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New transaction of the same stock is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val newPortfolio = portfolio.right.value.addTransaction(transaction2)

      Then("Portfolio should have single holding with that stock")
      newPortfolio.right.value.activeHoldings should contain only (Holding(transaction1.stock, transaction1.quantity), Holding(
        transaction2.stock,
        transaction2.quantity
      ))
    }
  }

  Feature("Adding sell transaction") {
    Scenario("No such stock") {
      val portfolio = Portfolio.empty

      When("New transaction is added")
      val transaction = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(10), someDate)
      val result = portfolio.addTransaction(transaction)

      Then("Portfolio should not be changed")
      result.left.value shouldBe (NotEnoughBalance(transaction.stock))
    }

    Scenario("Not enough stock") {
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New transaction is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(10), someDate)
      val result = portfolio.right.value.addTransaction(transaction2)

      Then("Portfolio should not be changed")
      result.left.value shouldBe (NotEnoughBalance(transaction2.stock))
    }

    Scenario("Just enough stock") {
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(5), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New transaction is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(5), someDate)
      val result = portfolio.right.value.addTransaction(transaction2)

      Then("Portfolio should be empty")
      result.right.value.activeHoldings shouldBe empty
    }

    Scenario("More than enough stock") {
      val transaction1 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Buy, Stock.Quantity(6), someDate)
      val portfolio = Portfolio.empty.addTransaction(transaction1)

      When("New transaction is added")
      val transaction2 = Transaction(Transaction.Id("1"), Stock.Id("OCDO"), Transaction.Type.Sell, Stock.Quantity(5), someDate)
      val result = portfolio.right.value.addTransaction(transaction2)

      Then("Portfolio should be empty")
      result.right.value.activeHoldings should contain only Holding(transaction1.stock, Quantity(1))
    }
  }

}
