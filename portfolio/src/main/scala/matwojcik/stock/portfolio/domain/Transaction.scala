package matwojcik.stock.portfolio.domain

import java.time.ZonedDateTime

import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity

case class Transaction(id: Transaction.Id, stock: Stock.Id, tpe: Transaction.Type, quantity: Quantity, date: ZonedDateTime)

object Transaction {
  case class Id(value: String)

  enum Type:
    case Buy, Sell

}
