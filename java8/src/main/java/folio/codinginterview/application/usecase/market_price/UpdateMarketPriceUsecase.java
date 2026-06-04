package folio.codinginterview.application.usecase.market_price;

import folio.codinginterview.application.repository.MarketPriceRepository;
import folio.codinginterview.domain.StockSymbol;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class UpdateMarketPriceUsecase {
    public static final class ItemInput {
        private final StockSymbol symbol;
        private final BigDecimal marketPrice;
        public ItemInput(StockSymbol symbol, BigDecimal marketPrice) {
            this.symbol = symbol;
            this.marketPrice = marketPrice;
        }
        public StockSymbol symbol() { return symbol; }
        public BigDecimal marketPrice() { return marketPrice; }
    }

    public static final class Input {
        private final List<ItemInput> items;
        public Input(List<ItemInput> items) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }
        public List<ItemInput> items() { return items; }
    }

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
