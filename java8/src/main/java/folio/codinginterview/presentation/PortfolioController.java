package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.portfolio.GetLatestPortfolioUsecase;
import folio.codinginterview.application.usecase.portfolio.UpdatePortfolioUsecase;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class PortfolioController {
    public static final class PortfolioItemDto {
        private final String symbol;
        private final String rate;
        public PortfolioItemDto(String symbol, String rate) {
            this.symbol = symbol;
            this.rate = rate;
        }
        public String symbol() { return symbol; }
        public String rate() { return rate; }
    }

    public static final class GetOptimalPortfolioResponse {
        private final List<PortfolioItemDto> portfolios;
        public GetOptimalPortfolioResponse(List<PortfolioItemDto> portfolios) {
            this.portfolios = Collections.unmodifiableList(new ArrayList<>(portfolios));
        }
        public List<PortfolioItemDto> portfolios() { return portfolios; }
    }

    public static final class UpdateOptimalPortfolioRequest {
        private final List<PortfolioItemDto> portfolios;
        public UpdateOptimalPortfolioRequest(List<PortfolioItemDto> portfolios) {
            this.portfolios = Collections.unmodifiableList(new ArrayList<>(portfolios));
        }
        public List<PortfolioItemDto> portfolios() { return portfolios; }
    }

    private final GetLatestPortfolioUsecase getLatestPortfolioUsecase;
    private final UpdatePortfolioUsecase updatePortfolioUsecase;

    public PortfolioController(
            GetLatestPortfolioUsecase getLatestPortfolioUsecase,
            UpdatePortfolioUsecase updatePortfolioUsecase
    ) {
        this.getLatestPortfolioUsecase = getLatestPortfolioUsecase;
        this.updatePortfolioUsecase = updatePortfolioUsecase;
    }

    public CompletableFuture<GetOptimalPortfolioResponse> getOptimalPortfolio() {
        return getLatestPortfolioUsecase.run().thenApply(out -> {
            List<PortfolioItemDto> items = new ArrayList<>();
            for (GetLatestPortfolioUsecase.ItemOutput i : out.items()) {
                items.add(new PortfolioItemDto(i.symbol().toString(), i.rate().toString()));
            }
            return new GetOptimalPortfolioResponse(items);
        });
    }

    public CompletableFuture<Void> updateOptimalPortfolio(UpdateOptimalPortfolioRequest req) {
        List<UpdatePortfolioUsecase.ItemInput> items = new ArrayList<>();
        for (PortfolioItemDto dto : req.portfolios()) {
            Optional<StockSymbol> sym = StockSymbol.fromString(dto.symbol());
            if (!sym.isPresent()) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new BadRequestException("unknown symbol: " + dto.symbol()));
                return failed;
            }
            BigDecimal rate;
            try {
                rate = new BigDecimal(dto.rate());
            } catch (RuntimeException e) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new BadRequestException("invalid rate: " + dto.rate()));
                return failed;
            }
            items.add(new UpdatePortfolioUsecase.ItemInput(sym.get(), rate));
        }
        return updatePortfolioUsecase.run(new UpdatePortfolioUsecase.Input(items))
                .handle((v, ex) -> {
                    if (ex != null) {
                        Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                        if (cause instanceof UpdatePortfolioUsecase.InvalidPortfolio) {
                            UpdatePortfolioUsecase.InvalidPortfolio ip = (UpdatePortfolioUsecase.InvalidPortfolio) cause;
                            throw new CompletionException(new BadRequestException(ip.getMessage()));
                        }
                        throw new CompletionException(cause);
                    }
                    return null;
                });
    }
}
