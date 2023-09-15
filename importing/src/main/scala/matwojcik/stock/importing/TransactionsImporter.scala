package matwojcik.stock.importing

import cats.effect.Sync
import cats.syntax.all._
import kantan.csv._
import kantan.csv.ops._

import java.net.URL
import java.time.ZoneId

trait TransactionsImporter[F[_]] {
  def readTransactions(url: URL): F[List[Transaction]]
}

object TransactionsImporter {
  def apply[F[_]](using ev: TransactionsImporter[F]): TransactionsImporter[F] = ev

  def instance[F[_]: Sync]: TransactionsImporter[F] =
    new TransactionsImporter[F] {

      override def readTransactions(url: URL): F[List[Transaction]] =
        Sync[F]
          .fromTry(ReadResult.sequence(url.readCsv[List, TransactionRow](rfc.withHeader)).toTry)
          .map(_.map(_.toTransaction(ZoneId.of("Europe/Warsaw"))))

    }

}
