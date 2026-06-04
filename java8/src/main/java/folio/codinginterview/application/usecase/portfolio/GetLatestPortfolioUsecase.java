package folio.codinginterview.application.usecase.portfolio;

import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.domain.PortfolioItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GetLatestPortfolioUsecase {
    public static final class ItemOutput {
        private final StockSymbol symbol;
        private final BigDecimal rate;
        public ItemOutput(StockSymbol symbol, BigDecimal rate) {
            this.symbol = symbol;
            this.rate = rate;
        }
        public StockSymbol symbol() { return symbol; }
        public BigDecimal rate() { return rate; }
    }

    public static final class Output {
        private final List<ItemOutput> items;
        public Output(List<ItemOutput> items) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }
        public List<ItemOutput> items() { return items; }
    }

    private final PortfolioRepository portfolioRepository;

    public GetLatestPortfolioUsecase(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public CompletableFuture<Output> run() {
        return portfolioRepository.get().thenApply(p -> {
            List<ItemOutput> items = new ArrayList<>();
            for (PortfolioItem i : p.items()) {
                items.add(new ItemOutput(i.symbol(), i.rate()));
            }
            return new Output(items);
        });
    }
}
