package matwojcik.stock

import cats.kernel.Monoid
import matwojcik.stock.domain.Stock.Quantity

import scala.math.BigDecimal.RoundingMode

object domain {

  object Stock {
    case class Id(value: String)

    case class Quantity(value: Int) {
      def plus(other: Quantity): Quantity = Quantity(value + other.value)
      def minus(other: Quantity): Quantity = Quantity(value - other.value)
      def >=(other: Quantity): Boolean = value >= other.value
    }

    case class Exchange(value: String)

  }

  case class Currency(id: String)
  case class CurrencyRate(value: BigDecimal)

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
