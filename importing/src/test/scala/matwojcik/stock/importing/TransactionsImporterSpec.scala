package matwojcik.stock.importing

import cats.effect.IO
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global

import java.io.File

class TransactionsImporterSpec extends AnyFeatureSpec with Matchers with GivenWhenThen {

  val importer: TransactionsImporter[IO] = TransactionsImporter.instance[IO]

  Scenario("easy ready") {
    importer.readTransactions(new File("/Users/mateusz.wojcik/Downloads/Transactions (2).csv").toURI.toURL).unsafeRunSync().foreach(println)
  }
}
