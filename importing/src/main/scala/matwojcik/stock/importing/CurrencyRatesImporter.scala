package matwojcik.stock.importing

import cats.effect.Sync
import kantan.csv._
import kantan.csv.ops._
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate

import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait CurrencyRatesImporter[F[_]] {
  def importCurrencyRates(url: URL): F[Map[LocalDate, Map[Currency, CurrencyRate]]]
}

object CurrencyRatesImporter {

  def apply[F[_]](implicit ev: CurrencyRatesImporter[F]): CurrencyRatesImporter[F] = ev

  // Imports PLN rates from NBP CSVs: https://www.nbp.pl/home.aspx?f=/kursy/arch_a.html
  def nbpInstance[F[_]: Sync]: CurrencyRatesImporter[F] =
    new CurrencyRatesImporter[F] {

      override def importCurrencyRates(url: URL): F[Map[LocalDate, Map[Currency, CurrencyRate]]] =
        Sync[F].delay(
          url
            .asCsvReader[CurrencyRatesRow](rfc.withHeader.withCellSeparator(';'))
            .collect { case Right(r) => r }
            .toList
            .map(toCurrencyMap)
            .toMap
        )

      private def toCurrencyMap(row: CurrencyRatesRow): (LocalDate, Map[Currency, CurrencyRate]) =
        row.date -> Map(
          Currency("EUR") -> CurrencyRate(row.eur),
          Currency("GBP") -> CurrencyRate(row.gbp),
          Currency("USD") -> CurrencyRate(row.usd),
          Currency("CHF") -> CurrencyRate(row.chf),
          Currency("GBX") -> CurrencyRate(row.gbp * 100)
        )

    }

  case class CurrencyRatesRow(date: LocalDate, eur: BigDecimal, gbp: BigDecimal, usd: BigDecimal, chf: BigDecimal)

  object CurrencyRatesRow {
    implicit val localDateDecoder: CellDecoder[LocalDate] =
      CellDecoder.from(s => DecodeResult(LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"))))
    implicit val bigDecimalDecoder: CellDecoder[BigDecimal] =
      CellDecoder.from(s => DecodeResult(BigDecimal(s.replace(',', '.'))))
    implicit val decoder: HeaderDecoder[CurrencyRatesRow] =
      HeaderDecoder.decoder("data", "1EUR", "1GBP", "1USD", "1CHF")(CurrencyRatesRow.apply)
  }

}
