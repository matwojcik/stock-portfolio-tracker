package matwojcik.stock.importing

import cats.implicits._
import kantan.csv._
import kantan.csv.java8._
import matwojcik.stock.domain.Stock.Quantity
import matwojcik.stock.domain.{Currency, Money, Stock}

import java.time.{LocalDate, LocalTime, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

case class TransactionRow(
  id: String,
  date: LocalDate,
  time: LocalTime,
  stockId: String,
  quantity: Int,
  stockPrice: BigDecimal,
  stockCurrency: String,
  cost: Option[BigDecimal],
  costCurrency: Option[String]
) {
  def toTransaction(zoneId: ZoneId): Transaction =
    Transaction(
      id = Transaction.Id(id),
      date = ZonedDateTime.of(date, time, zoneId).toInstant,
      tpe = if(quantity > 0) Transaction.Type.Buy else Transaction.Type.Sell,
      stockId = Stock.Id(stockId),
      stockPrice = Money(stockPrice, Currency(stockCurrency)),
      quantity = Quantity(quantity.abs),
      cost = (cost, costCurrency).mapN((value, currency) => Money(value.abs, Currency(currency))).getOrElse(Money(0, Currency(stockCurrency)))
    )
}

object TransactionRow {
  implicit val localDateDecoder: CellDecoder[LocalDate] =
    CellDecoder.from(s => DecodeResult(LocalDate.parse(s, DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
  implicit val decoder = RowDecoder.decoder(18, 0, 1, 3, 6, 7, 8, 14, 15)(TransactionRow.apply)
}
