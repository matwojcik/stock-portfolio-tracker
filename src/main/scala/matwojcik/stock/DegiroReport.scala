package matwojcik.stock

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.all._
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate
import matwojcik.stock.importing.CurrencyRatesImporter
import matwojcik.stock.importing.TransactionsImporter
import matwojcik.stock.taxes.domain.Income
import matwojcik.stock.taxes.domain.IncomeCalculator
import matwojcik.stock.taxes.domain.Transaction

import java.net.URL
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId

trait DegiroReport[F[_]] {
  def calculateIncome(year: Year, transactionsUrl: URL, currencies: NonEmptyList[URL]): F[List[Income]]
}

object DegiroReport {
  def apply[F[_]](implicit ev: DegiroReport[F]): DegiroReport[F] = ev

  private val polishZone: ZoneId = ZoneId.of("Europe/Warsaw")
  private val PLN: Currency = Currency("PLN")

  def instance[F[_]: TransactionsImporter: CurrencyRatesImporter: IncomeCalculator: Sync]: DegiroReport[F] =
    new DegiroReport[F] {

      override def calculateIncome(year: Year, transactionsUrl: URL, currencies: NonEmptyList[URL]): F[List[Income]] =
        for {
          transactions <- TransactionsImporter[F].readTransactions(transactionsUrl)
          currencies   <- currencies.traverse(CurrencyRatesImporter[F].importCurrencyRates).map(_.toList.flatten.toMap)
          taxTransactions = transactions.map(toTaxTransaction(_, currencies))
          income       <- NonEmptyList
                            .fromList(taxTransactions)
                            .toOptionT[F]
                            .semiflatMap(IncomeCalculator[F].calculate(year, polishZone, PLN, _))
                            .getOrElse(Nil)
        } yield income

      private def toTaxTransaction(t: importing.Transaction, currencyRates: Map[LocalDate, Map[Currency, CurrencyRate]]): Transaction = {
        val localDate =
          t.date
            .atZone(polishZone)
            .minusDays(1)
            .toLocalDate // according to polish law previous day exchange rate needs to be taken

        // if previous day comes in some bank holiday
        val adjustedDate = currencyRates.keys.toList.sorted.reverse.find(_ isBefore localDate)

        val currencies = currencyRates.getOrElse(
          localDate,
          adjustedDate.flatMap(currencyRates.get).get // todo effectful
        )

        val stockExchangeRate =
          if (t.stockPrice.currency == PLN) CurrencyRate(1.0)
          else currencies.get(t.stockPrice.currency).get // todo effectful

        val provisionExchangeRate =
          if (t.provision.currency == PLN) CurrencyRate(1.0)
          else currencies.get(t.provision.currency).get // todo effectful

        Transaction(
          id = Transaction.Id(t.id.value),
          stock = t.stockId,
          tpe = if (t.tpe == importing.Transaction.Type.Sell) Transaction.Type.Sell else Transaction.Type.Buy,
          quantity = t.quantity,
          stockPrice = t.stockPrice,
          stockPriceExchangeRate = stockExchangeRate,
          provision = t.provision,
          provisionExchangeRate = provisionExchangeRate,
          date = t.date
        )
      }

    }

}
