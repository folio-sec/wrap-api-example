package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase;
import folio.codinginterview.application.usecase.order.NewOrderUsecase;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class OrderController extends PresentationPreparation {
    public record NewOrderRequest(String userId, String amount) {}

    public record AdditionalOrderRequest(String userId, String amount) {}

    private final NewOrderUsecase newOrderUsecase;
    private final AdditionalBuyOrderUsecase additionalBuyOrderUsecase;

    public OrderController(
            NewOrderUsecase newOrderUsecase,
            AdditionalBuyOrderUsecase additionalBuyOrderUsecase
    ) {
        this.newOrderUsecase = newOrderUsecase;
        this.additionalBuyOrderUsecase = additionalBuyOrderUsecase;
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
}
