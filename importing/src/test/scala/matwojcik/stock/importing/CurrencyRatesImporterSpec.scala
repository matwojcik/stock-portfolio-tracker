package matwojcik.stock.importing

import cats.effect.IO
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global

import java.io.File

class CurrencyRatesImporterSpec extends AnyFeatureSpec with Matchers with GivenWhenThen {

  val importer: CurrencyRatesImporter[IO] = CurrencyRatesImporter.nbpInstance[IO]

  Scenario("easy ready") {
    importer
      .importCurrencyRates(new File("/Users/mateusz.wojcik/Downloads/archiwum_tab_a_2020.csv").toURI.toURL)
      .unsafeRunSync()
      .foreach(println)
  }
}
