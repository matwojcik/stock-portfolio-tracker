package matwojcik.stock.taxes.domain

import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity

import java.time.Instant

case class Transaction(
  id: Transaction.Id,
  stock: Stock.Id,
  exchange: Stock.Exchange,
  tpe: Transaction.Type,
  quantity: Quantity,
  stockPrice: Money,
  stockPriceExchangeRate: CurrencyRate,
  provision: Money,
  provisionExchangeRate: CurrencyRate,
  date: Instant
) {
  def totalStockCost: Money = (stockPrice * quantity).rounded
  // todo how to make it less problematic around currency?
  def totalStockCostInAccountingCurrency(currency: Currency): Money = totalStockCost.to(currency)(stockPriceExchangeRate).rounded
  def provisionInAccountingCurrency(currency: Currency): Money = provision.to(currency)(provisionExchangeRate).rounded
}

object Transaction {
  case class Id(value: String)

  enum Type:
    case Buy, Sell

}
