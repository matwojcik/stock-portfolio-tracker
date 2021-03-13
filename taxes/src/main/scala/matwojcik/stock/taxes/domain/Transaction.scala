package matwojcik.stock.taxes.domain

import io.estatico.newtype.macros.newtype
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.domain.CurrencyRate
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

import java.time.Instant

case class Transaction(
  id: Transaction.Id,
  stock: Stock.Id,
  tpe: Transaction.Type,
  quantity: Quantity,
  stockPrice: Money,
  stockPriceExchangeRate: CurrencyRate,
  cost: Money,
  costExchangeRate: CurrencyRate,
  date: Instant
)

object Transaction {
  @newtype case class Id(value: String)

  sealed trait Type extends Product with Serializable

  object Type {
    case object Buy extends Type
    case object Sell extends Type
  }

}
