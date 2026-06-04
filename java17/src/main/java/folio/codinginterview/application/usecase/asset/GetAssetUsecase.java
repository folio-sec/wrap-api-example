package folio.codinginterview.application.usecase.asset;

import folio.codinginterview.application.repository.AccountRepository;
import folio.codinginterview.application.repository.MarketPriceRepository;
import folio.codinginterview.application.service.AssetService;
import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.domain.UserId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GetAssetUsecase {
    public record Input(UserId userId) {}

    public record StockOutput(StockSymbol symbol, BigDecimal evaluationAmount) {}

    public record Output(BigDecimal cashAmount, List<StockOutput> stocks) {}

    public static class Exception extends RuntimeException {
        protected Exception(String msg) { super(msg); }
    }

    public static final class UserNotFound extends Exception {
        public static final UserNotFound INSTANCE = new UserNotFound();
        private UserNotFound() { super("user not found"); }
    }

    private final AccountRepository accountRepository;
    private final MarketPriceRepository marketPriceRepository;

    public GetAssetUsecase(AccountRepository accountRepository, MarketPriceRepository marketPriceRepository) {
        this.accountRepository = accountRepository;
        this.marketPriceRepository = marketPriceRepository;
    }

    public CompletableFuture<Output> run(Input input) {
        return accountRepository.find(input.userId()).thenCompose(maybeAccount -> {
            if (maybeAccount.isEmpty()) {
                CompletableFuture<Output> failed = new CompletableFuture<>();
                failed.completeExceptionally(UserNotFound.INSTANCE);
                return failed;
            }
            Account account = maybeAccount.get();
            return marketPriceRepository.all().thenApply(prices -> {
                List<StockOutput> stocks = new ArrayList<>();
                for (var e : account.stocks()) {
                    stocks.add(new StockOutput(e.symbol(), AssetService.evaluateStock(e, prices)));
                }
                return new Output(account.cash(), stocks);
            });
        });
    }
}
