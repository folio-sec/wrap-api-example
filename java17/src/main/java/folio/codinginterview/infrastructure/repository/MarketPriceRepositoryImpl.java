package folio.codinginterview.infrastructure.repository;

import folio.codinginterview.application.repository.MarketPriceRepository;
import folio.codinginterview.domain.AppConstants;
import folio.codinginterview.domain.StockSymbol;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class MarketPriceRepositoryImpl implements MarketPriceRepository {
    private final AtomicReference<Map<StockSymbol, BigDecimal>> ref =
            new AtomicReference<>(AppConstants.INITIAL_PRICES);

    @Override
    public CompletableFuture<Map<StockSymbol, BigDecimal>> all() {
        return CompletableFuture.completedFuture(ref.get());
    }

    @Override
    public CompletableFuture<Void> update(Map<StockSymbol, BigDecimal> prices) {
        ref.set(Map.copyOf(prices));
        return CompletableFuture.completedFuture(null);
    }
}
