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

    Scenario("no sells in a year") {
      val transactions = NonEmptyList.of(
        buyTransaction
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions shouldBe empty
    }

    Scenario("simple equal transactions") {
      val transactions = NonEmptyList.of(
        sellTransaction,
        buyTransaction
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction))
    }

    Scenario("multiple stocks, single sell") {
      val transactions = NonEmptyList.of(
        sellTransaction,
        buyTransaction,
        buyTransaction.copy(id = Transaction.Id("B2"), stock = otherStockId, date = February2020)
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction))
    }

    Scenario("multiple buys, single taken"){
      Given("Multiple buy transactions, both bigger than sell")
      val transactions = NonEmptyList.of(
        sellTransaction,
        buyTransaction,
        buy(id = Transaction.Id("B2"),quantity = Quantity(30), price = 20.0, date = February2020)
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      Then("First in time transaction should be taken")
      soldPositions should contain only Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction))
    }


    Scenario("multiple buys, multiple taken"){
      Given("Multiple buy transactions, first smaller than sell")
      val sell = sellTransaction.copy(quantity = Quantity(20))
      val secondBuy = buy(id = Transaction.Id("B2"), quantity = Quantity(30), price = 20.0, date = February2020)
      val transactions = NonEmptyList.of(
        sell,
        buyTransaction,
        secondBuy
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      Then("First transaction should be fully used, and the second only partially")
      soldPositions should contain only Income.SoldPosition(sell, NonEmptyList.of(buyTransaction, secondBuy.copy(quantity = Quantity(10))))
    }


    Scenario("multiple stocks") {
      val sellStock2 = sellTransaction.copy(id = Transaction.Id("S2"), stock = otherStockId, date = March2020)
      val buyStock2 = buyTransaction.copy(id = Transaction.Id("B2"), stock = otherStockId, date = December2019)
      val transactions = NonEmptyList.of(
        buyStock2,
        buyTransaction,
        sellTransaction,
        sellStock2
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only (Income.SoldPosition(sellTransaction, NonEmptyList.of(buyTransaction)), Income.SoldPosition(sellStock2, NonEmptyList.of(buyStock2)))
    }


    Scenario("multiple sells in the same year") {
      val firstBuy = buy(id = Transaction.Id("B1"), quantity = Quantity(30), date = September2019)
      val secondBuy = buy(id = Transaction.Id("B2"), quantity = Quantity(20), date = October2019)
      val firstSell = sell(id = Transaction.Id("S1"), quantity = Quantity(10), date = January2020)
      val secondSell = sell(id = Transaction.Id("S2"), quantity = Quantity(25), date = February2020)
      val thirdSell = sell(id = Transaction.Id("S3"), quantity = Quantity(15), date = March2020)

      val transactions = NonEmptyList.of(
        firstBuy,
        secondBuy,
        firstSell,
        secondSell,
        thirdSell
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only (
        Income.SoldPosition(firstSell, NonEmptyList.of(firstBuy.copy(quantity = Quantity(10)))),
        Income.SoldPosition(secondSell, NonEmptyList.of(firstBuy.copy(quantity = Quantity(20)), secondBuy.copy(quantity = Quantity(5)))),
        Income.SoldPosition(thirdSell, NonEmptyList.of(secondBuy.copy(quantity = Quantity(15)))),
        )
    }

    Scenario("sell in previous years") {
      val firstBuy = buy(id = Transaction.Id("B1"), quantity = Quantity(30), date = September2019)
      val secondBuy = buy(id = Transaction.Id("B2"), quantity = Quantity(20), date = October2019)
      val firstSell = sell(id = Transaction.Id("S1"), quantity = Quantity(10), date = November2019)
      val secondSell = sell(id = Transaction.Id("S2"), quantity = Quantity(30), date = February2020)

      val transactions = NonEmptyList.of(
        firstBuy,
        secondBuy,
        firstSell,
        secondSell
      )

      val soldPositions = instance.findSoldPositions(Y2020, PolishZone, transactions).unsafeRunSync()

      soldPositions should contain only (Income.SoldPosition(secondSell, NonEmptyList.of(firstBuy.copy(quantity = Quantity(20)), secondBuy.copy(quantity = Quantity(10)))))
    }

  }
}
