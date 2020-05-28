package matwojcik.stock.history.domain

import java.time.LocalDate

import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

case class StockPriceHistory(id: Stock.Id, price: Money, date: LocalDate)
