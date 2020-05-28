package matwojcik.stock.portfolio.domain

import java.time.ZonedDateTime

import io.estatico.newtype.macros.newtype
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity

case class Transaction(id: Transaction.Id, stock: Stock.Id, tpe: Transaction.Type, quantity: Quantity, date: ZonedDateTime)

object Transaction {
  @newtype case class Id(value: String)

  sealed trait Type extends Product with Serializable

  object Type {
    case object Buy extends Type
    case object Sell extends Type
  }

}
