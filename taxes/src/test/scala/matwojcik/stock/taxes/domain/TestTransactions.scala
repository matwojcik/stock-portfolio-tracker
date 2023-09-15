package matwojcik.stock.taxes.domain

import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

import java.time.Instant
import java.time.OffsetDateTime
import java.time.Year
import java.time.ZoneId
import java.time.ZoneOffset

object TestTransactions {
  val someStockId: Stock.Id = Stock.Id("STOCK1")
  val otherStockId: Stock.Id = Stock.Id("STOCK2")
  val EUR: Currency = Currency("EUR")
  val PLN: Currency = Currency("PLN")

  val EurToPln: CurrencyRate = CurrencyRate(0.25)
  val Y2019: Year = Year.of(2019)
  val Y2020: Year = Year.of(2020)
  val PolishZone: ZoneId = ZoneId.of("Europe/Warsaw")
  val September2019: Instant = Instant.from(OffsetDateTime.of(2019, 9, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val October2019: Instant = Instant.from(OffsetDateTime.of(2019, 10, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val November2019: Instant = Instant.from(OffsetDateTime.of(2019, 11, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val December2019: Instant = Instant.from(OffsetDateTime.of(2019, 12, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val January2020: Instant = Instant.from(OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val February2020: Instant = Instant.from(OffsetDateTime.of(2020, 2, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val March2020: Instant = Instant.from(OffsetDateTime.of(2020, 3, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))

  val sellTransaction: Transaction = Transaction(
    id = Transaction.Id("S1"),
    stock = someStockId,
    exchange = Stock.Exchange("LSE"),
    tpe = Transaction.Type.Sell,
    quantity = Stock.Quantity(10),
    stockPrice = Money(50.0, EUR),
    stockPriceExchangeRate = EurToPln,
    provision = Money(1, EUR),
    provisionExchangeRate = EurToPln,
    date = March2020
  )

  val buyTransaction: Transaction = sellTransaction.copy(id = Transaction.Id("B1"), tpe = Transaction.Type.Buy, date = December2019)

  def sell(
    id: Transaction.Id,
    stockId: Stock.Id = someStockId,
    quantity: Quantity = Quantity(10),
    price: BigDecimal = 50.0,
    date: Instant = February2020
  ): Transaction =
    sellTransaction.copy(id = id, stock = stockId, quantity = quantity, stockPrice = Money(price, EUR), date = date)

  def buy(
    id: Transaction.Id,
    stockId: Stock.Id = someStockId,
    quantity: Quantity = Quantity(10),
    price: BigDecimal = 50.0,
    date: Instant = February2020
  ): Transaction =
    buyTransaction.copy(id = id, stock = stockId, quantity = quantity, stockPrice = Money(price, EUR), date = date)

}
