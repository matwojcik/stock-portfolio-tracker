package matwojcik.stock.taxes.domain

import cats.data.NonEmptyList
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Money
import matwojcik.stock.taxes.domain.Income.SoldPosition

import java.time.Instant

case class Income(date: Instant, netIncome: Money, soldPosition: SoldPosition)

object Income {

  case class SoldPosition(sellTransaction: Transaction, buyTransactions: NonEmptyList[Transaction]) {

    def grossIncome(currency: Currency): Money = sellTransaction.totalStockCostInAccountingCurrency(currency)

    def netIncome(currency: Currency): Money =
      grossIncome(currency) minusUnsafe totalCost(currency)

    def totalCost(currency: Currency): Money =
      totalBuyCost(currency) plusUnsafe sellTransaction.provisionInAccountingCurrency(currency)

    def totalBuyCost(currency: Currency): Money =
      (buyTransactions
        .map(_.totalStockCostInAccountingCurrency(currency)) ::: buyTransactions
        .map(_.provisionInAccountingCurrency(currency)))
        .reduce(Money.monoid.plus(currency))

  }

}
