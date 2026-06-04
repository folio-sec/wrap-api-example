package folio.codinginterview.application.repository;

import folio.codinginterview.domain.StockSymbol;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 市場価格リポジトリ。
 */
public interface MarketPriceRepository {
    CompletableFuture<Map<StockSymbol, BigDecimal>> all();

    CompletableFuture<Void> update(Map<StockSymbol, BigDecimal> prices);
}
