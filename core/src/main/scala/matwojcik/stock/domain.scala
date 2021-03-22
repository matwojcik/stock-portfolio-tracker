package matwojcik.stock

import io.estatico.newtype.macros.newtype
import matwojcik.stock.domain.Stock.Quantity

import scala.math.BigDecimal.RoundingMode

object domain {

  object Stock {
    @newtype case class Id(value: String)

    @newtype case class Quantity(value: Int) {
      def plus(other: Quantity): Quantity = Quantity(value + other.value)
      def minus(other: Quantity): Quantity = Quantity(value - other.value)
      def >=(other: Quantity): Boolean = value >= other.value
    }

  }

  @newtype case class Currency(id: String)
  @newtype case class CurrencyRate(value: BigDecimal)

  case class Money(value: BigDecimal, currency: Currency) {
    def *(quantity: Quantity): Money = Money(quantity.value * value, currency)
    def rounded: Money = Money(value.setScale(2, RoundingMode.HALF_UP), currency)

    // todo maybe it could be done type safe?
    def minus(other: Money): Option[Money] = Option.when(other.currency == currency)(Money(value - other.value, currency))
    def plus(other: Money): Option[Money] = Option.when(other.currency == currency)(Money(value + other.value, currency))

    def to(anotherCurrency: Currency)(currencyRate: CurrencyRate): Money =
      Money(value * currencyRate.value, anotherCurrency)
  }

}
