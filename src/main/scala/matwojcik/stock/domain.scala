package matwojcik.stock

import io.estatico.newtype.macros.newtype

object domain {

  object Stock {
    @newtype case class Id(value: String)

    @newtype case class Quantity(value: Int) {
      def plus(other: Quantity): Quantity = Quantity(value + other.value)
      def minus(other: Quantity): Quantity = Quantity(value - other.value)
      def >=(other: Quantity): Boolean = value >= other.value
    }

  }

}
