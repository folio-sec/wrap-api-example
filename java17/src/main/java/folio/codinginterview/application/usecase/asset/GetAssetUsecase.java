package folio.codinginterview.application.usecase.asset;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.domain.UserId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GetAssetUsecase {
    public record Input(UserId userId) {}

    public record StockOutput(StockSymbol symbol, BigDecimal amountJpy) {}

    public record Output(BigDecimal cashAmount, List<StockOutput> stocks) {}

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class UserNotFound extends Exception {
        public static final UserNotFound INSTANCE = new UserNotFound();
        private UserNotFound() { super("user not found"); }
    }

    private final AccountRepository accountRepository;

    public GetAssetUsecase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public CompletableFuture<Output> run(Input input) {
        return accountRepository.find(input.userId()).thenCompose(maybeAccount -> {
            if (maybeAccount.isEmpty()) {
                CompletableFuture<Output> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserNotFound.INSTANCE);
                return failed;
            }
            Account account = maybeAccount.get();
            List<StockOutput> stocks = new ArrayList<>();
            for (var e : account.stocks()) {
                stocks.add(new StockOutput(e.symbol(), e.amountJpy()));
            }
            return CompletableFuture.completedFuture(new Output(account.cash(), stocks));
        });
    }
}
