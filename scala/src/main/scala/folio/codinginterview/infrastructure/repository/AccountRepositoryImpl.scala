package folio.codinginterview.infrastructure.repository

import folio.codinginterview.application.repository.AccountRepository
import folio.codinginterview.domain.Account
import folio.codinginterview.domain.UserId
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

final class AccountRepositoryImpl extends AccountRepository {
  private val store: TrieMap[String, Account] = TrieMap.empty

  override def find(userId: UserId): Future[Option[Account]] =
    Future.successful(store.get(userId.value))

  override def upsert(userId: UserId, account: Account): Future[Unit] = {
    store.update(userId.value, account)
    Future.unit
  }

  override def exists(userId: UserId): Future[Boolean] =
    Future.successful(store.contains(userId.value))
}
