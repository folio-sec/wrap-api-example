package folio.codinginterview.application.repository;

import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.UserId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 口座管理リポジトリ。
 */
public interface AccountRepository {
    CompletableFuture<Optional<Account>> find(UserId userId);

    CompletableFuture<Void> upsert(UserId userId, Account account);

    CompletableFuture<Boolean> exists(UserId userId);
}
