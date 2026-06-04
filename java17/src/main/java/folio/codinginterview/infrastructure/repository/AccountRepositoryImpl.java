package folio.codinginterview.infrastructure.repository;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.UserId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AccountRepositoryImpl implements AccountRepository {
    private final ConcurrentMap<String, Account> store = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Optional<Account>> find(UserId userId) {
        return CompletableFuture.completedFuture(Optional.ofNullable(store.get(userId.value())));
    }

    @Override
    public CompletableFuture<Void> upsert(UserId userId, Account account) {
        store.put(userId.value(), account);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Boolean> exists(UserId userId) {
        return CompletableFuture.completedFuture(store.containsKey(userId.value()));
    }
}
