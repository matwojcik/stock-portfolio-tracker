package matwojcik.stock.history.domain

import java.time.LocalDate

import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate

trait Currencies[F[_]] {
  def findCurrencyRate(from: Currency, to: Currency, date: LocalDate): F[CurrencyRate]
}

object Currencies {
  def apply[F[_]](using ev: Currencies[F]): Currencies[F] = ev
}
