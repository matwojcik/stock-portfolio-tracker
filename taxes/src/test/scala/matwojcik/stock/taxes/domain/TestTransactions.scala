package matwojcik.stock.taxes.domain

import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate
import matwojcik.stock.domain.Money
import matwojcik.stock.domain.Stock

import java.time.{Instant, OffsetDateTime, Year, ZoneId, ZoneOffset}

object TestTransactions {
  val someStockId: Stock.Id = Stock.Id("STOCK1")
  val otherStockId: Stock.Id = Stock.Id("STOCK2")
  val EUR: Currency = Currency("EUR")

  val EurToPln: CurrencyRate = CurrencyRate(0.25)
  val Y2020 = Year.of(2020)
  val PolishZone = ZoneId.of("Europe/Warsaw")
  val December2019 = Instant.from(OffsetDateTime.of(2019, 12, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val February2020 = Instant.from(OffsetDateTime.of(2020, 2, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))
  val March2020 = Instant.from(OffsetDateTime.of(2020, 3, 1, 10, 0, 0, 0, ZoneOffset.ofHours(1)))

  val sellTransaction = Transaction(
    stock = someStockId,
    tpe = Transaction.Type.Sell,
    quantity = Stock.Quantity(10),
    stockPrice = Money(50.0, EUR),
    stockPriceExchangeRate = EurToPln,
    cost = Money(1, EUR),
    costExchangeRate = EurToPln,
    date = March2020
  )

  val buyTransaction = sellTransaction.copy(tpe = Transaction.Type.Buy, date = December2019)

  def sell(stockId: Stock.Id = someStockId, quantity: Quantity, price: BigDecimal, date: Instant): Transaction =
    sellTransaction.copy(stock = stockId, quantity = quantity, stockPrice = Money(price, EUR), date = date)

  def buy(stockId: Stock.Id = someStockId, quantity: Quantity, price: BigDecimal, date: Instant): Transaction =
    buyTransaction.copy(stock = stockId, quantity = quantity, stockPrice = Money(price, EUR), date = date)
}
