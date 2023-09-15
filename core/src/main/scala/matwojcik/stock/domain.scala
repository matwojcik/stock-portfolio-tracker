package matwojcik.stock

import cats.kernel.Monoid
import matwojcik.stock.domain.Stock.Quantity

import scala.math.BigDecimal.RoundingMode

object domain {

  object Stock {
    opaque type Id = String

    object Id {
      def apply(value: String): Id = value
    }

    opaque type Quantity = Int

    object Quantity {
      def apply(value: Int): Quantity = value
    }

    extension (value: Quantity) {
      def plus(other: Quantity): Quantity = value + other
      def minus(other: Quantity): Quantity = value - other
      def >=(other: Quantity): Boolean = value >= other
      def value: Int = value
    }

    opaque type Exchange = String

    object Exchange {
      def apply(value: String): Exchange = value
    }

  }

  opaque type Currency = String

  object Currency {
    def apply(value: String): Currency = value
  }

  opaque type CurrencyRate = BigDecimal

  object CurrencyRate {
    def apply(value: BigDecimal): CurrencyRate = value
  }

  extension (cr: CurrencyRate) {
    def value: BigDecimal = cr.bigDecimal
  }

  case class Money(value: BigDecimal, currency: Currency) {
    def *(quantity: Quantity): Money = Money(quantity.value * value, currency)
    def *(multiplier: Double): Money = Money(multiplier * value, currency)
    def rounded: Money = Money(value.setScale(2, RoundingMode.HALF_UP), currency)

    // todo maybe it could be done type safe?
    def minus(other: Money): Option[Money] = Option.when(other.currency == currency)(minusUnsafe(other))
    def minusUnsafe(other: Money): Money = Money(value - other.value, currency)
    def plus(other: Money): Option[Money] = Option.when(other.currency == currency)(plusUnsafe(other))
    def plusUnsafe(other: Money): Money = Money(value + other.value, currency)

    def to(anotherCurrency: Currency)(currencyRate: CurrencyRate): Money =
      Money(value * currencyRate.value, anotherCurrency)

  }

  object Money {

    object monoid {

      def plus(currency: Currency): Monoid[Money] =
        new Monoid[Money] {
          override def combine(x: Money, y: Money): Money =
            x plusUnsafe y

          override def empty: Money = Money(0, currency)
        }

    }

  }

}
