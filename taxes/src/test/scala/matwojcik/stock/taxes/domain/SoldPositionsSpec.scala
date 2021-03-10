package matwojcik.stock.taxes.domain

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import TestTransactions._
import cats.Id
import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.IO
import matwojcik.stock.domain.Stock.Quantity
import org.scalatest.GivenWhenThen

class SoldPositionsSpec extends AnyFeatureSpec with Matchers with GivenWhenThen{

  val instance = SoldPositions.instance[IO]

  Feature("finding sold positions"){
    Scenario("simple equal transactions") {
      val transactions = NonEmptyList.of(
        sellTransaction,
        buyTransaction
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction))
    }

    Scenario("multiple stocks") {
      val transactions = NonEmptyList.of(
        sellTransaction,
        buyTransaction,
        buyTransaction.copy(stock = otherStockId, date = February2020)
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction))
    }

    Scenario("multiple buys, single taken"){
      Given("Multiple buy transactions, both bigger than sell")
      val transactions = NonEmptyList.of(
        sellTransaction,
        buyTransaction,
        buy(quantity = Quantity(30), price = 20.0, date = February2020)
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      Then("First in time transaction should be taken")
      soldPositions should contain only Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction))
    }


    Scenario("multiple buys, multiple taken"){
      Given("Multiple buy transactions, first smaller than sell")
      val sell = sellTransaction.copy(quantity = Quantity(20))
      val secondBuy = buy(quantity = Quantity(30), price = 20.0, date = February2020)
      val transactions = NonEmptyList.of(
        sell,
        buyTransaction,
        secondBuy
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      Then("First transaction should be fully used, and the second only partially")
      soldPositions should contain only Income.SoldPosition(sell, NonEmptyList.of(buyTransaction, secondBuy.copy(quantity = Quantity(10))))
    }
  }
}
