package folio.codinginterview.application.usecase.order;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.application.repository.MarketPriceRepository;
import folio.codinginterview.application.repository.PortfolioRepository;
import folio.codinginterview.application.service.PortfolioService;
import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.UserId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class RebalanceOrderUsecase {
    public static final class Input {
        private final UserId userId;
        public Input(UserId userId) { this.userId = userId; }
        public UserId userId() { return userId; }
    }

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class UserNotFound extends Exception {
        public static final UserNotFound INSTANCE = new UserNotFound();
        private UserNotFound() { super("user not found"); }
    }

    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final MarketPriceRepository marketPriceRepository;

    public RebalanceOrderUsecase(
            AccountRepository accountRepository,
            PortfolioRepository portfolioRepository,
            MarketPriceRepository marketPriceRepository
    ) {
        this.accountRepository = accountRepository;
        this.portfolioRepository = portfolioRepository;
        this.marketPriceRepository = marketPriceRepository;
    }

    public CompletableFuture<Void> run(Input input) {
        return accountRepository.find(input.userId()).thenCompose((Optional<Account> maybeAccount) -> {
            if (!maybeAccount.isPresent()) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserNotFound.INSTANCE);
                return failed;
            }
            Account account = maybeAccount.get();
            return portfolioRepository.get().thenCompose(portfolio ->
                    marketPriceRepository.all().thenCompose(prices -> {
                        Account updated = PortfolioService.rebalance(account, portfolio, prices);
                        return accountRepository.upsert(input.userId(), updated);
                    }));
        });
    }
}
