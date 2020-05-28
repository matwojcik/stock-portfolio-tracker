package matwojcik.stock.history.domain

import java.time.LocalDate

import io.estatico.newtype.macros.newtype
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.CurrencyRate

trait Currencies[F[_]] {
  def findCurrencyRate(from: Currency, to: Currency, date: LocalDate): F[CurrencyRate]
}

object Currencies {
  def apply[F[_]](implicit ev: Currencies[F]): Currencies[F] = ev
}
