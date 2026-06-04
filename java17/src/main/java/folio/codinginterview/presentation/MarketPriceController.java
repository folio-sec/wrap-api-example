package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.market_price.UpdateMarketPriceUsecase;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class MarketPriceController {
    public record MarketPriceItemDto(String symbol, String market_price) {}

    public record UpdateMarketPriceRequest(List<MarketPriceItemDto> market_prices) {}

    private final UpdateMarketPriceUsecase updateMarketPriceUsecase;

    public MarketPriceController(UpdateMarketPriceUsecase updateMarketPriceUsecase) {
        this.updateMarketPriceUsecase = updateMarketPriceUsecase;
    }

    public CompletableFuture<Void> updateMarketPrice(UpdateMarketPriceRequest req) {
        List<UpdateMarketPriceUsecase.ItemInput> items = new ArrayList<>();
        for (var dto : req.market_prices()) {
            Optional<StockSymbol> sym = StockSymbol.fromString(dto.symbol());
            if (sym.isEmpty()) {
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
