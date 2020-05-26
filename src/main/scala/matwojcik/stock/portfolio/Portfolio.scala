package matwojcik.stock.portfolio

import java.time.ZonedDateTime

import io.estatico.newtype.macros.newtype
import cats.implicits._
import matwojcik.stock.domain.Stock
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.portfolio.Portfolio.failures.NotEnoughBalance
import matwojcik.stock.portfolio.Transaction.Type

case class Portfolio private (private val holdings: Map[Stock.Id, Holding]) {

  def addTransaction(transaction: Transaction): Either[NotEnoughBalance, Portfolio] = {
    val stock = transaction.stock
    val quantity = transaction.quantity
    val newHolding = transaction.tpe match {
      case Type.Buy =>
        addStock(stock, quantity).asRight[NotEnoughBalance]
      case Type.Sell =>
        holdings.get(stock) match {
          case Some(holding) =>
            Either.cond(holding.balance >= quantity, holding.copy(balance = holding.balance minus quantity), NotEnoughBalance(stock))
          case None =>
            NotEnoughBalance(stock).asLeft[Holding]
        }
    }

    newHolding.map(holding =>
      Portfolio(holdings = (holdings + (stock -> holding)).filter { case (_, holding) => holding.balance.value > 0 })
    )
  }

  private def addStock(stock: Stock.Id, quantity: Quantity) =
    holdings.get(stock).fold(Holding(stock, quantity))(holding => holding.copy(balance = holding.balance plus quantity))

  def activeHoldings: List[Holding] = holdings.values.toList

  private def copy(): Unit = ()
}

object Portfolio {
  def empty: Portfolio = Portfolio(Map.empty)

  object failures {
    case class NotEnoughBalance(stock: Stock.Id)
  }
}

case class Holding(stock: Stock.Id, balance: Quantity)

case class Transaction(id: Transaction.Id, stock: Stock.Id, tpe: Transaction.Type, quantity: Quantity, date: ZonedDateTime)

object Transaction {
  @newtype case class Id(value: String)

  sealed trait Type extends Product with Serializable

  object Type {
    case object Buy extends Type
    case object Sell extends Type
  }

}
