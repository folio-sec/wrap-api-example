package folio.codinginterview.presentation;

import folio.codinginterview.domain.UserId;
import folio.codinginterview.presentation.PresentationException.BadRequestException;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public abstract class PresentationPreparation {
    protected CompletableFuture<UserId> parseUserId(String s) {
        try {
            return CompletableFuture.completedFuture(new UserId(s));
        } catch (RuntimeException e) {
            CompletableFuture<UserId> failed = new CompletableFuture<>();
            failed.completeExceptionally(new BadRequestException(e.getMessage()));
            return failed;
        }
    }

    protected CompletableFuture<BigDecimal> parseAmount(String s) {
        try {
            return CompletableFuture.completedFuture(new BigDecimal(s));
        } catch (RuntimeException e) {
            CompletableFuture<BigDecimal> failed = new CompletableFuture<>();
            failed.completeExceptionally(new BadRequestException("invalid amount: " + s));
            return failed;
        }
    }
}
