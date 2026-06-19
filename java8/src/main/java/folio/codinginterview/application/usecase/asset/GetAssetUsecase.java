package folio.codinginterview.application.usecase.asset;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.Stock;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.domain.UserId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class GetAssetUsecase {
    public static final class Input {
        private final UserId userId;
        public Input(UserId userId) { this.userId = userId; }
        public UserId userId() { return userId; }
    }

    public static final class StockOutput {
        private final StockSymbol symbol;
        private final BigDecimal amountJpy;
        public StockOutput(StockSymbol symbol, BigDecimal amountJpy) {
            this.symbol = symbol;
            this.amountJpy = amountJpy;
        }
        public StockSymbol symbol() { return symbol; }
        public BigDecimal amountJpy() { return amountJpy; }
    }

    public static final class Output {
        private final BigDecimal cashAmount;
        private final List<StockOutput> stocks;
        public Output(BigDecimal cashAmount, List<StockOutput> stocks) {
            this.cashAmount = cashAmount;
            this.stocks = Collections.unmodifiableList(new ArrayList<>(stocks));
        }
        public BigDecimal cashAmount() { return cashAmount; }
        public List<StockOutput> stocks() { return stocks; }
    }

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
        return accountRepository.find(input.userId()).thenCompose((Optional<Account> maybeAccount) -> {
            if (!maybeAccount.isPresent()) {
                CompletableFuture<Output> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserNotFound.INSTANCE);
                return failed;
            }
            Account account = maybeAccount.get();
            List<StockOutput> stocks = new ArrayList<>();
            for (Stock e : account.stocks()) {
                stocks.add(new StockOutput(e.symbol(), e.amountJpy()));
            }
            return CompletableFuture.completedFuture(new Output(account.cash(), stocks));
        });
    }
}
