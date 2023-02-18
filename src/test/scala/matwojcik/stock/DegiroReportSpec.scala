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
        Year.of(2022),
        new File("/Users/mateusz/projects/stock-data/2022/Transactions.csv").toURI.toURL,
        NonEmptyList.of(
          new File("/Users/mateusz/projects/stock-data/2022/archiwum_tab_a_2019.csv").toURI.toURL,
          new File("/Users/mateusz/projects/stock-data/2022/archiwum_tab_a_2020.csv").toURI.toURL,
          new File("/Users/mateusz/projects/stock-data/2022/archiwum_tab_a_2021.csv").toURI.toURL,
          new File("/Users/mateusz/projects/stock-data/2022/archiwum_tab_a_2022.csv").toURI.toURL
        )
      )
      .unsafeRunSync()

    result.foreach(income => println(income.show))

    def printSum(name: String)(income: List[Income])(f: Income => Money) = 
      println(s"$name: " + income.map(f).combineAll(Money.monoid.plus(Currency("PLN"))))

    def printReport(incomes: List[Income]) = {
      printSum("Net income")(incomes)(_.netIncome)
      printSum("Gross income")(incomes)(_.soldPosition.grossIncome(Currency("PLN")))
      printSum("Total cost")(incomes)(_.soldPosition.totalCost(Currency("PLN")))
    }

    result.groupBy(_.soldPosition.sellTransaction.exchange).foreach {
      case (exchange, value) =>
        println(s"StockExchange: $exchange")
        printReport(value)
        println("------------")
    }

    println(s"Total")
    printReport(result)
    println("------------")

  }

  implicit val incomeShow: Show[Income] = Show.show {
    case Income(date, value, soldPosition) =>
      show"""Income at $date for ${soldPosition.sellTransaction.stock.value} at ${soldPosition.sellTransaction.exchange.value}: $value  (exchange rate: ${soldPosition.sellTransaction.stockPriceExchangeRate.value})
            |Sell: ${soldPosition.sellTransaction}
            |Buys:
            |""".stripMargin ++ soldPosition.buyTransactions.map(t => show"$t").reduceLeft(_ ++ "\n" ++ _) ++ "\n"
  }

  implicit val moneyShow: Show[Money] = Show.show(money => s"${money.value} ${money.currency}")
  implicit val transactionShow: Show[Transaction] = 
    Show.show(transaction => show"${transaction.quantity.value} shares for ${transaction.stockPrice} with provision ${transaction.provision} at ${transaction.date} (exchange rate: ${transaction.stockPriceExchangeRate.value})")
    // Show.fromToString
  implicit val instantShow: Show[Instant] = Show.fromToString
}
