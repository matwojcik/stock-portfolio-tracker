package matwojcik.stock.portfolio.domain

import matwojcik.stock.domain.Stock

trait TransactionRepository[F[_]] {
  def findAllTransactions(portfolio: Portfolio.Id, stock: Stock.Id): F[List[Transaction]]
}

object TransactionRepository {
  def apply[F[_]](implicit ev: TransactionRepository[F]): TransactionRepository[F] = ev
}
