package matwojcik.stock

import cats.Show
import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.showInterpolator
import matwojcik.stock.domain.Currency
import matwojcik.stock.domain.Money
import matwojcik.stock.importing.CurrencyRatesImporter
import matwojcik.stock.importing.TransactionsImporter
import matwojcik.stock.taxes.domain.Income
import matwojcik.stock.taxes.domain.IncomeCalculator
import matwojcik.stock.taxes.domain.SoldPositions
import matwojcik.stock.taxes.domain.Transaction
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.time.Instant
import java.time.Year

class DegiroReportSpec extends AnyFeatureSpec with Matchers with GivenWhenThen {
  implicit val sp = SoldPositions.instance[IO]
  implicit val calculator = IncomeCalculator.instance[IO]
  implicit val importer = CurrencyRatesImporter.nbpInstance[IO]
  implicit val transactionImporter = TransactionsImporter.instance[IO]
  val instance = DegiroReport.instance[IO]

  Scenario("Import") {

    val result = instance
      .calculateIncome(
        Year.of(2019),
        new File("/Users/mateusz.wojcik/projects/stock-data/2020/Transactions (2).csv").toURI.toURL,
        NonEmptyList.of(
          new File("/Users/mateusz.wojcik/projects/stock-data/2020/archiwum_tab_a_2019.csv").toURI.toURL,
          new File("/Users/mateusz.wojcik/projects/stock-data/2020/archiwum_tab_a_2020.csv").toURI.toURL,
          new File("/Users/mateusz.wojcik/projects/stock-data/2020/archiwum_tab_a_2021.csv").toURI.toURL
        )
      )
      .unsafeRunSync()

    result.foreach(income => println(income.show))

    def printSum(income: List[Income])(f: Income => Money) = println(income.map(f).combineAll(Money.monoid.plus(Currency("PLN"))))

    result.groupBy(_.soldPosition.sellTransaction.exchange).foreach {
      case (exchange, value) =>
        println(s"StockExchange: $exchange")
        printSum(value)(_.netIncome)
        printSum(value)(_.soldPosition.grossIncome(Currency("PLN")))
        printSum(value)(_.soldPosition.totalCost(Currency("PLN")))
        println("------------")
    }

    println(s"Total")
    printSum(result)(_.netIncome)
    printSum(result)(_.soldPosition.grossIncome(Currency("PLN")))
    printSum(result)(_.soldPosition.totalCost(Currency("PLN")))
    println("------------")

  }

  implicit val incomeShow: Show[Income] = Show.show {
    case Income(date, value, soldPosition) =>
      show"""$date: $value
            |Sell: ${soldPosition.sellTransaction}
            |Buys:
            |""".stripMargin ++ soldPosition.buyTransactions.map(t => show"$t").reduceLeft(_ ++ "\n" ++ _) ++ "\n"
  }

  implicit val moneyShow: Show[Money] = Show.show(money => s"${money.value} ${money.currency}")
  implicit val transactionShow: Show[Transaction] = Show.fromToString
  implicit val instantShow: Show[Instant] = Show.fromToString
}
