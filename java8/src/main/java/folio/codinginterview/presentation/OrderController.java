package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase;
import folio.codinginterview.application.usecase.order.NewOrderUsecase;
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class OrderController extends PresentationPreparation {
    public static final class NewOrderRequest {
        private final String userId;
        private final String amount;
        public NewOrderRequest(String userId, String amount) {
            this.userId = userId;
            this.amount = amount;
        }
        public String userId() { return userId; }
        public String amount() { return amount; }
    }

    public static final class AdditionalOrderRequest {
        private final String userId;
        private final String amount;
        public AdditionalOrderRequest(String userId, String amount) {
            this.userId = userId;
            this.amount = amount;
        }
        public String userId() { return userId; }
        public String amount() { return amount; }
    }

    public static final class RebalanceOrderRequest {
        private final String userId;
        public RebalanceOrderRequest(String userId) { this.userId = userId; }
        public String userId() { return userId; }
    }

    private final NewOrderUsecase newOrderUsecase;
    private final AdditionalBuyOrderUsecase additionalBuyOrderUsecase;
    private final RebalanceOrderUsecase rebalanceOrderUsecase;

    public OrderController(
            NewOrderUsecase newOrderUsecase,
            AdditionalBuyOrderUsecase additionalBuyOrderUsecase,
            RebalanceOrderUsecase rebalanceOrderUsecase
    ) {
        this.newOrderUsecase = newOrderUsecase;
        this.additionalBuyOrderUsecase = additionalBuyOrderUsecase;
        this.rebalanceOrderUsecase = rebalanceOrderUsecase;
    }

    public CompletableFuture<Void> newOrder(NewOrderRequest req) {
        return parseUserId(req.userId()).thenCompose(uid ->
                parseAmount(req.amount()).thenCompose(amt ->
                        newOrderUsecase.run(new NewOrderUsecase.Input(uid, amt))
                                .handle((v, ex) -> {
                                    if (ex != null) {
                                        Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                                        if (cause instanceof NewOrderUsecase.UserAlreadyExists) {
                                            throw new CompletionException(new BadRequestException("user already has account"));
                                        }
                                        if (cause instanceof NewOrderUsecase.AmountTooSmall) {
                                            throw new CompletionException(new BadRequestException("amount is too small"));
                                        }
                                        throw new CompletionException(cause);
                                    }
                                    return null;
                                })));
    }

    public CompletableFuture<Void> additionalOrder(AdditionalOrderRequest req) {
        return parseUserId(req.userId()).thenCompose(uid ->
                parseAmount(req.amount()).thenCompose(amt ->
                        additionalBuyOrderUsecase.run(new AdditionalBuyOrderUsecase.Input(uid, amt))
                                .handle((v, ex) -> {
                                    if (ex != null) {
                                        Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                                        if (cause instanceof AdditionalBuyOrderUsecase.UserNotFound) {
                                            throw new CompletionException(new BadRequestException("user has no live account"));
                                        }
                                        if (cause instanceof AdditionalBuyOrderUsecase.AmountTooSmall) {
                                            throw new CompletionException(new BadRequestException("amount is too small"));
                                        }
                                        throw new CompletionException(cause);
                                    }
                                    return null;
                                })));
    }

    public CompletableFuture<Void> rebalanceOrder(RebalanceOrderRequest req) {
        return parseUserId(req.userId()).thenCompose(uid ->
                rebalanceOrderUsecase.run(new RebalanceOrderUsecase.Input(uid))
                        .handle((v, ex) -> {
                            if (ex != null) {
                                Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                                if (cause instanceof RebalanceOrderUsecase.UserNotFound) {
                                    throw new CompletionException(new BadRequestException("user has no live account"));
                                }
                                throw new CompletionException(cause);
                            }
                            return null;
                        }));
    }
}
