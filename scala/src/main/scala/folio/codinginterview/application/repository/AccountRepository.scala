package folio.codinginterview.application.repository

import folio.codinginterview.domain.Account
import folio.codinginterview.domain.UserId
import scala.concurrent.Future

/** 口座管理リポジトリ。 */
trait AccountRepository {
  def find(userId: UserId): Future[Option[Account]]
  def upsert(userId: UserId, account: Account): Future[Unit]
  def exists(userId: UserId): Future[Boolean]
}
