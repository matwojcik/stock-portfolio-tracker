package matwojcik.stock.taxes.domain

import cats.data.NonEmptyList
import cats.effect.IO
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.taxes.domain.Income.SoldPosition
import matwojcik.stock.taxes.domain.TestTransactions._
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

class IncomeCalculatorSpec extends AnyFeatureSpec with Matchers with GivenWhenThen{
  implicit val sp = SoldPositions.instance[IO]
  implicit val calculator = IncomeCalculator.instance[IO]

  Feature("Calculating income") {
    Scenario("No transactions in year") {
      val transactions = NonEmptyList.of(
        buyTransaction
      )

      val soldPositions = calculator.calculate(Y2020, PolishZone, PLN, transactions).unsafeRunSync()

      soldPositions shouldBe empty
    }

    Scenario("Only buy transactions in year") {
      val transactions = NonEmptyList.of(
        buyTransaction
      )

      val soldPositions = calculator.calculate(Y2019, PolishZone, PLN, transactions).unsafeRunSync()

      soldPositions should contain only Income.Provision(buyTransaction.date, buyTransaction.cost.to(PLN)(buyTransaction.costExchangeRate) * Quantity(-1), buyTransaction)
    }


    Scenario("Single buy single sell") {
      val buyTransaction = buy(id = Transaction.Id("B1"), quantity = Quantity(10), date = September2019)
      val sellTransaction = sell(id = Transaction.Id("B1"), quantity = Quantity(10), date = January2020)
      val transactions = NonEmptyList.of(
        buyTransaction,
        sellTransaction,
      )

      val soldPositions = calculator.calculate(Y2020, PolishZone, PLN, transactions).unsafeRunSync()

      val sellIncome = sellTransaction.totalStockCostInAccountingCurrency(PLN) minus buyTransaction.totalStockCostInAccountingCurrency(PLN)

      soldPositions should contain only (
        Income.Provision(sellTransaction.date, sellTransaction.cost.to(PLN)(sellTransaction.costExchangeRate) * Quantity(-1), sellTransaction),
        Income.StockSell(sellTransaction.date, sellIncome.get, SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction)))
        )
    }
  }
}