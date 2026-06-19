package folio.codinginterview.application.usecase.order;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.AppConstants;
import folio.codinginterview.domain.UserId;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public final class NewOrderUsecase {
    public record Input(UserId userId, BigDecimal amount) {}

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class UserAlreadyExists extends Exception {
        public static final UserAlreadyExists INSTANCE = new UserAlreadyExists();
        private UserAlreadyExists() { super("user already exists"); }
    }

    public static final class AmountTooSmall extends Exception {
        public static final AmountTooSmall INSTANCE = new AmountTooSmall();
        private AmountTooSmall() { super("amount too small"); }
    }

    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;

    public NewOrderUsecase(AccountRepository accountRepository, PortfolioRepository portfolioRepository) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public CompletableFuture<Void> run(Input input) {
        if (input.amount().compareTo(AppConstants.MIN_OPERATION_AMOUNT) < 0) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(AmountTooSmall.INSTANCE);
            return failed;
        }
        return accountRepository.exists(input.userId()).thenCompose(exists -> {
            if (exists) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserAlreadyExists.INSTANCE);
                return failed;
            }
            return portfolioRepository.get().thenCompose(portfolio -> {
                var account = Account.openAccount(input.amount(), portfolio);
                return accountRepository.upsert(input.userId(), account);
            });
        });
    }
}
