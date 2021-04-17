package matwojcik.stock.importing

import io.estatico.newtype.macros.newtype
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
  @newtype case class Id(value: String)

  sealed trait Type extends Product with Serializable

  object Type {
    case object Buy extends Type
    case object Sell extends Type
  }

}
