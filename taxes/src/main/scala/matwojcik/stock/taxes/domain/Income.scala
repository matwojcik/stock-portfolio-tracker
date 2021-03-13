package matwojcik.stock.taxes.domain

import cats.data.NonEmptyList
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Money

import java.time.Instant

sealed trait Income {
  def date: Instant
  def value: Money
}

object Income {

  case class StockSell(date: Instant, value: Money, soldPosition: SoldPosition) extends Income
  case class Provision(date: Instant, value: Money, transaction: Transaction) extends Income

  case class SoldPosition(sellTransaction: Transaction, buyTransactions: NonEmptyList[Transaction]) {

    def totalCost(currency: Currency): Money = sellTransaction.totalStockCostInAccountingCurrency(currency)

    def income(currency: Currency): Money = (totalCost(currency) minus totalBuyCost(currency)).get

    private def totalBuyCost(currency: Currency) =
      buyTransactions
        .map(_.totalStockCostInAccountingCurrency(currency))
        .reduceLeft[Money] {
          case (l, r) => (l plus r).get
        }

  }

}
