package matwojcik.stock.importing

import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity

import java.time.Instant

case class Transaction(
  id: Transaction.Id,
  date: Instant,
  tpe: Transaction.Type,
  stockId: Stock.Id,
  exchange: Stock.Exchange,
  stockPrice: Money,
  quantity: Quantity,
  provision: Money
)

object Transaction {
  case class Id(value: String)

  enum Type:
    case Buy, Sell

}
