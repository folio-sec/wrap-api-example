package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.asset.GetAssetUsecase;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class AssetController extends PresentationPreparation {
    public static final class StockDto {
        private final String symbol;
        private final String evaluationAmount;
        public StockDto(String symbol, String evaluationAmount) {
            this.symbol = symbol;
            this.evaluationAmount = evaluationAmount;
        }
        public String symbol() { return symbol; }
        public String evaluationAmount() { return evaluationAmount; }
    }

    public static final class GetAssetRequest {
        private final String userId;
        public GetAssetRequest(String userId) { this.userId = userId; }
        public String userId() { return userId; }
    }

    public static final class GetAssetResponse {
        private final String cashAmount;
        private final List<StockDto> stocks;
        public GetAssetResponse(String cashAmount, List<StockDto> stocks) {
            this.cashAmount = cashAmount;
            this.stocks = Collections.unmodifiableList(new ArrayList<>(stocks));
        }
        public String cashAmount() { return cashAmount; }
        public List<StockDto> stocks() { return stocks; }
    }

    private final GetAssetUsecase getAssetUsecase;

    public AssetController(GetAssetUsecase getAssetUsecase) {
        this.getAssetUsecase = getAssetUsecase;
    }

    public CompletableFuture<GetAssetResponse> getAsset(GetAssetRequest req) {
        return parseUserId(req.userId()).thenCompose(uid ->
                getAssetUsecase.run(new GetAssetUsecase.Input(uid))
                        .handle((out, ex) -> {
                            if (ex != null) {
                                Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                                if (cause instanceof GetAssetUsecase.UserNotFound) {
                                    throw new CompletionException(new BadRequestException("user not found"));
                                }
                                throw new CompletionException(cause);
                            }
                            List<StockDto> stocks = new ArrayList<>();
                            for (GetAssetUsecase.StockOutput e : out.stocks()) {
                                stocks.add(new StockDto(e.symbol().toString(), e.evaluationAmount().toString()));
                            }
                            return new GetAssetResponse(out.cashAmount().toString(), stocks);
                        }));
    }
}
