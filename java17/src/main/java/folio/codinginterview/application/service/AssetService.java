package folio.codinginterview.application.service;

import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.Stock;
import folio.codinginterview.domain.StockSymbol;

import java.math.BigDecimal;
import java.util.Map;

public final class AssetService {
    private AssetService() {}

    public static BigDecimal evaluateStock(Stock stock, Map<StockSymbol, BigDecimal> prices) {
        BigDecimal price = prices.get(stock.symbol());
        if (price == null) {
            throw new IllegalStateException("missing price for " + stock.symbol());
        }
        return stock.qty().multiply(price);
    }

    public static BigDecimal totalValuation(Account account, Map<StockSymbol, BigDecimal> prices) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Stock e : account.stocks()) {
            sum = sum.add(evaluateStock(e, prices));
        }
        return sum.add(account.cash());
    }
}
