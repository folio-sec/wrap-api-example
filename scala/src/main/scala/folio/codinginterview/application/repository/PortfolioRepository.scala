package folio.codinginterview.application.repository

import folio.codinginterview.domain.Portfolio
import scala.concurrent.Future

/** 最適ポートフォリオリポジトリ。 */
trait PortfolioRepository {
  def get(): Future[Portfolio]
  def update(portfolio: Portfolio): Future[Unit]
}
