package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceUsecase;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class MarketPriceController {
    public static final class MarketPriceItemDto {
        private final String symbol;
        private final String market_price;
        public MarketPriceItemDto(String symbol, String market_price) {
            this.symbol = symbol;
            this.market_price = market_price;
        }
        public String symbol() { return symbol; }
        public String market_price() { return market_price; }
    }

    public static final class UpdateMarketPriceRequest {
        private final List<MarketPriceItemDto> market_prices;
        public UpdateMarketPriceRequest(List<MarketPriceItemDto> market_prices) {
            this.market_prices = Collections.unmodifiableList(new ArrayList<>(market_prices));
        }
        public List<MarketPriceItemDto> market_prices() { return market_prices; }
    }

    private final UpdateMarketPriceUsecase updateMarketPriceUsecase;

    public MarketPriceController(UpdateMarketPriceUsecase updateMarketPriceUsecase) {
        this.updateMarketPriceUsecase = updateMarketPriceUsecase;
    }

    public CompletableFuture<Void> updateMarketPrice(UpdateMarketPriceRequest req) {
        List<UpdateMarketPriceUsecase.ItemInput> items = new ArrayList<>();
        for (MarketPriceItemDto dto : req.market_prices()) {
            Optional<StockSymbol> sym = StockSymbol.fromString(dto.symbol());
            if (!sym.isPresent()) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new BadRequestException("unknown symbol: " + dto.symbol()));
                return failed;
            }
            BigDecimal price;
            try {
                price = new BigDecimal(dto.market_price());
            } catch (RuntimeException e) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new BadRequestException("invalid market_price: " + dto.market_price()));
                return failed;
            }
            items.add(new UpdateMarketPriceUsecase.ItemInput(sym.get(), price));
        }
        return updateMarketPriceUsecase.run(new UpdateMarketPriceUsecase.Input(items));
    }
}
