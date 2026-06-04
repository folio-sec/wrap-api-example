package folio.codinginterview.infrastructure.repository;

import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.AppConstants;
import folio.codinginterview.domain.Portfolio;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class PortfolioRepositoryImpl implements PortfolioRepository {
    private final AtomicReference<Portfolio> ref = new AtomicReference<>(AppConstants.INITIAL_PORTFOLIO);

    @Override
    public CompletableFuture<Portfolio> get() {
        return CompletableFuture.completedFuture(ref.get());
    }

    @Override
    public CompletableFuture<Void> update(Portfolio portfolio) {
        ref.set(portfolio);
        return CompletableFuture.completedFuture(null);
    }
}
