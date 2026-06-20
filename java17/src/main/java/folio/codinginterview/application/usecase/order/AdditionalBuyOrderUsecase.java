package folio.codinginterview.application.usecase.order;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.AppConstants;
import folio.codinginterview.domain.UserId;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public final class AdditionalBuyOrderUsecase {
    public record Input(UserId userId, BigDecimal amount) {}

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class UserNotFound extends Exception {
        public static final UserNotFound INSTANCE = new UserNotFound();
        private UserNotFound() { super("user not found"); }
    }

    public static final class AmountTooSmall extends Exception {
        public static final AmountTooSmall INSTANCE = new AmountTooSmall();
        private AmountTooSmall() { super("amount too small"); }
    }

    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;

    public AdditionalBuyOrderUsecase(
            AccountRepository accountRepository,
            PortfolioRepository portfolioRepository
    ) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public CompletableFuture<Void> run(Input input) {
        if (input.amount().compareTo(AppConstants.MIN_OPERATION_AMOUNT) < 0) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(AmountTooSmall.INSTANCE);
            return failed;
        }
        return accountRepository.find(input.userId()).thenCompose(maybeAccount -> {
            if (maybeAccount.isEmpty()) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserNotFound.INSTANCE);
                return failed;
            }
            var account = maybeAccount.get();
            return portfolioRepository.get().thenCompose(portfolio -> {
                var updated = account.addFunds(input.amount(), portfolio);
                return accountRepository.upsert(input.userId(), updated);
            });
        });
    }
}
