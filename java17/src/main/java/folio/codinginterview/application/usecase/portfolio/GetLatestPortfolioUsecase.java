package folio.codinginterview.application.usecase.portfolio;

import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.StockSymbol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GetLatestPortfolioUsecase {
    public record ItemOutput(StockSymbol symbol, BigDecimal rate) {}

    public record Output(List<ItemOutput> items) {}

    private final PortfolioRepository portfolioRepository;

    public GetLatestPortfolioUsecase(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public CompletableFuture<Output> run() {
        return portfolioRepository.get().thenApply(p -> {
            List<ItemOutput> items = new ArrayList<>();
            for (var i : p.items()) {
                items.add(new ItemOutput(i.symbol(), i.rate()));
            }
            return new Output(items);
        });
    }
}
