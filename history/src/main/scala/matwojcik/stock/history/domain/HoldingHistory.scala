package matwojcik.stock.history.domain

import java.time.LocalDate

import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

case class HoldingHistory(
  portfolio: Portfolio.Id,
  stock: Stock.Id,
  date: LocalDate,
  quantity: Quantity,
  price: Money,
  totalValue: Money,
  totalValueInPortfolioCurrency: Money
)

object Portfolio {
  case class Id(value: String)
}
