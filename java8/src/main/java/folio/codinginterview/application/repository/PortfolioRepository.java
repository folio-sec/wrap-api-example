package folio.codinginterview.application.repository;

import folio.codinginterview.domain.Portfolio;

import java.util.concurrent.CompletableFuture;

/**
 * 最適ポートフォリオリポジトリ。
 */
public interface PortfolioRepository {
    CompletableFuture<Portfolio> get();

    CompletableFuture<Void> update(Portfolio portfolio);
}
