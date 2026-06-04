package folio.codinginterview.presentation;

import folio.codinginterview.application.usecase.order.AdditionalBuyOrderUsecase;
import folio.codinginterview.application.usecase.order.NewContributionOrderUsecase;
import folio.codinginterview.application.usecase.order.RebalanceOrderUsecase;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class OrderController extends PresentationPreparation {
    public record NewContributionOrderRequest(String userId, String amount) {}

    public record AdditionalContributionOrderRequest(String userId, String amount) {}

    public record RebalanceOrderRequest(String userId) {}

    private final NewContributionOrderUsecase newContributionOrderUsecase;
    private final AdditionalBuyOrderUsecase additionalBuyOrderUsecase;
    private final RebalanceOrderUsecase rebalanceOrderUsecase;

    public OrderController(
            NewContributionOrderUsecase newContributionOrderUsecase,
            AdditionalBuyOrderUsecase additionalBuyOrderUsecase,
            RebalanceOrderUsecase rebalanceOrderUsecase
    ) {
        this.newContributionOrderUsecase = newContributionOrderUsecase;
        this.additionalBuyOrderUsecase = additionalBuyOrderUsecase;
        this.rebalanceOrderUsecase = rebalanceOrderUsecase;
    }

    public CompletableFuture<Void> newContributionOrder(NewContributionOrderRequest req) {
        return parseUserId(req.userId()).thenCompose(uid ->
                parseAmount(req.amount()).thenCompose(amt ->
                        newContributionOrderUsecase.run(new NewContributionOrderUsecase.Input(uid, amt))
                                .handle((v, ex) -> {
                                    if (ex != null) {
                                        Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                                        if (cause instanceof NewContributionOrderUsecase.UserAlreadyExists) {
                                            throw new CompletionException(new BadRequestException("user already has account"));
                                        }
                                        if (cause instanceof NewContributionOrderUsecase.AmountTooSmall) {
                                            throw new CompletionException(new BadRequestException("amount is too small"));
                                        }
                                        throw new CompletionException(cause);
                                    }
                                    return null;
                                })));
    }

    public CompletableFuture<Void> additionalContributionOrder(AdditionalContributionOrderRequest req) {
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
