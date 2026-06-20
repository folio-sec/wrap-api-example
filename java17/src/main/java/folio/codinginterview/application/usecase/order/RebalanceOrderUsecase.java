package folio.codinginterview.application.usecase.order;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.domain.UserId;

import java.util.concurrent.CompletableFuture;

public final class RebalanceOrderUsecase {
    public record Input(UserId userId) {}

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class UserNotFound extends Exception {
        public static final UserNotFound INSTANCE = new UserNotFound();
        private UserNotFound() { super("user not found"); }
    }

    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;

    public RebalanceOrderUsecase(
            AccountRepository accountRepository,
            PortfolioRepository portfolioRepository
    ) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public CompletableFuture<Void> run(Input input) {
        return accountRepository.find(input.userId()).thenCompose(maybeAccount -> {
            if (maybeAccount.isEmpty()) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserNotFound.INSTANCE);
                return failed;
            }
            var account = maybeAccount.get();
            return portfolioRepository.get().thenCompose(portfolio -> {
                var updated = account.rebalance(portfolio);
                return accountRepository.upsert(input.userId(), updated);
            });
        });
    }
}
