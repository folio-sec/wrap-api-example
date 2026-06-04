package folio.codinginterview.application.usecase.market_price;

import folio.codinginterview.application.repository.MarketPriceRepository;
import folio.codinginterview.domain.StockSymbol;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class UpdateMarketPriceUsecase {
    public record ItemInput(StockSymbol symbol, BigDecimal marketPrice) {}

    public record Input(List<ItemInput> items) {}

    private final MarketPriceRepository marketPriceRepository;

    public UpdateMarketPriceUsecase(MarketPriceRepository marketPriceRepository) {
        this.marketPriceRepository = marketPriceRepository;
    }

    public CompletableFuture<Void> run(Input input) {
        Map<StockSymbol, BigDecimal> prices = new LinkedHashMap<>();
        for (ItemInput i : input.items()) {
            prices.put(i.symbol(), i.marketPrice());
        }
        return marketPriceRepository.update(prices);
    }
}
