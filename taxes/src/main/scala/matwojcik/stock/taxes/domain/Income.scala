package matwojcik.stock.taxes.domain

import cats.data.NonEmptyList
import matwojcik.stock.domain.Money

import java.time.Instant

case class Income(date: Instant, tpe: Income.Type, value: Money)

object Income {
  sealed trait Type extends Product with Serializable
  object Type {
    case object StockSell extends Type
    case object Provision extends Type
  }
  case class SoldPosition(sellTransaction: Transaction, buyTransactions: NonEmptyList[Transaction])
}
