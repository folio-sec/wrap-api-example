package folio.codinginterview.application.usecase.portfolio;

import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.domain.Portfolio;
import folio.codinginterview.domain.PortfolioItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class UpdatePortfolioUsecase {
    public static final class ItemInput {
        private final StockSymbol symbol;
        private final BigDecimal rate;
        public ItemInput(StockSymbol symbol, BigDecimal rate) {
            this.symbol = symbol;
            this.rate = rate;
        }
        public StockSymbol symbol() { return symbol; }
        public BigDecimal rate() { return rate; }
    }

    public static final class Input {
        private final List<ItemInput> items;
        public Input(List<ItemInput> items) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }
        public List<ItemInput> items() { return items; }
    }

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class InvalidPortfolio extends Exception {
        public InvalidPortfolio(String reason) { super(reason); }
    }

    private final PortfolioRepository portfolioRepository;

    public UpdatePortfolioUsecase(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public CompletableFuture<Void> run(Input input) {
        Portfolio portfolio;
        try {
            List<PortfolioItem> items = new ArrayList<>();
            for (ItemInput i : input.items()) {
                items.add(new PortfolioItem(i.symbol(), i.rate()));
            }
            portfolio = new Portfolio(items);
        } catch (RuntimeException e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new InvalidPortfolio(e.getMessage()));
            return failed;
        }
        return portfolioRepository.update(portfolio);
    }
}
