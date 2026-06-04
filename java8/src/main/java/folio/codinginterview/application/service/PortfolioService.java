package folio.codinginterview.application.service;

import folio.codinginterview.domain.Account;
import folio.codinginterview.domain.AppConstants;
import folio.codinginterview.domain.Stock;
import folio.codinginterview.domain.StockSymbol;
import folio.codinginterview.domain.Portfolio;
import folio.codinginterview.domain.PortfolioItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PortfolioService {
    private PortfolioService() {}

    private static BigDecimal floor2(BigDecimal x) {
        return x.setScale(2, RoundingMode.DOWN);
    }

    private static BigDecimal floor0(BigDecimal x) {
        return x.setScale(0, RoundingMode.DOWN);
    }

    private static BigDecimal priceOf(Map<StockSymbol, BigDecimal> prices, StockSymbol symbol) {
        BigDecimal p = prices.get(symbol);
        if (p == null) {
            throw new IllegalStateException("missing price for " + symbol);
        }
        return p;
    }

    /** Allocate a brand-new account given a contribution amount. */
    public static Account allocateNew(
            BigDecimal amount,
            Portfolio portfolio,
            Map<StockSymbol, BigDecimal> prices
    ) {
        BigDecimal cashFromRate = floor0(amount.multiply(AppConstants.CASH_RATE));
        BigDecimal investable = amount.subtract(cashFromRate);
        List<Stock> stocks = new ArrayList<>();
        for (PortfolioItem item : portfolio.items()) {
            BigDecimal price = priceOf(prices, item.symbol());
            BigDecimal qty = floor2(investable.multiply(item.rate()).divide(price, 20, RoundingMode.DOWN));
            stocks.add(new Stock(item.symbol(), qty));
        }
        BigDecimal usedForStocks = BigDecimal.ZERO;
        for (Stock e : stocks) {
            usedForStocks = usedForStocks.add(e.qty().multiply(priceOf(prices, e.symbol())));
        }
        BigDecimal residual = investable.subtract(usedForStocks);
        return new Account(cashFromRate.add(residual), stocks);
    }

    /** Additional contribution: only buy (no sell). Residual is kept in cash. */
    public static Account allocateAdditional(
            Account account,
            BigDecimal amount,
            Portfolio portfolio,
            Map<StockSymbol, BigDecimal> prices
    ) {
        BigDecimal totalAfter = AssetService.totalValuation(account, prices).add(amount);
        BigDecimal targetCash = floor0(totalAfter.multiply(AppConstants.CASH_RATE));
        BigDecimal investable = totalAfter.subtract(targetCash);

        Map<StockSymbol, BigDecimal> currentQty = new HashMap<>();
        for (Stock e : account.stocks()) {
            currentQty.put(e.symbol(), e.qty());
        }

        Set<StockSymbol> portfolioSymbols = new HashSet<>();
        for (PortfolioItem item : portfolio.items()) {
            portfolioSymbols.add(item.symbol());
        }

        List<Stock> newPortfolioStocks = new ArrayList<>();
        for (PortfolioItem item : portfolio.items()) {
            BigDecimal price = priceOf(prices, item.symbol());
            BigDecimal targetQty = floor2(investable.multiply(item.rate()).divide(price, 20, RoundingMode.DOWN));
            BigDecimal current = currentQty.getOrDefault(item.symbol(), BigDecimal.ZERO);
            BigDecimal finalQty = targetQty.compareTo(current) > 0 ? targetQty : current;
            newPortfolioStocks.add(new Stock(item.symbol(), finalQty));
        }

        List<Stock> preservedStocks = new ArrayList<>();
        for (Stock e : account.stocks()) {
            if (!portfolioSymbols.contains(e.symbol())) {
                preservedStocks.add(e);
            }
        }

        List<Stock> allStocks = new ArrayList<>();
        allStocks.addAll(newPortfolioStocks);
        allStocks.addAll(preservedStocks);

        BigDecimal finalValuation = BigDecimal.ZERO;
        for (Stock e : allStocks) {
            finalValuation = finalValuation.add(e.qty().multiply(priceOf(prices, e.symbol())));
        }
        BigDecimal finalCash = totalAfter.subtract(finalValuation);
        return new Account(finalCash, allStocks);
    }

    /** Rebalance: re-allocate qty per portfolio target (buy and sell). */
    public static Account rebalance(
            Account account,
            Portfolio portfolio,
            Map<StockSymbol, BigDecimal> prices
    ) {
        // XXX this implementation might not be correct
        BigDecimal investable = AssetService.totalValuation(account, prices);
        List<Stock> newStocks = new ArrayList<>();
        for (PortfolioItem item : portfolio.items()) {
            BigDecimal price = priceOf(prices, item.symbol());
            BigDecimal qty = floor2(investable.multiply(item.rate()).divide(price, 20, RoundingMode.DOWN));
            newStocks.add(new Stock(item.symbol(), qty));
        }
        BigDecimal finalValuation = BigDecimal.ZERO;
        for (Stock e : newStocks) {
            finalValuation = finalValuation.add(e.qty().multiply(priceOf(prices, e.symbol())));
        }
        BigDecimal finalCash = investable.subtract(finalValuation);
        return new Account(finalCash, newStocks);
    }
}
