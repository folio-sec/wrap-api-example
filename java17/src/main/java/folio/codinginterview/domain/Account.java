package folio.codinginterview.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 口座を表す。 */
public record Account(BigDecimal cash, List<Stock> stocks) {
    public Account {
        stocks = List.copyOf(stocks);
    }

    /** 円未満を切り捨てる（資産配分はすべて円単位で行う）。 */
    private static BigDecimal floor0(BigDecimal x) {
        return x.setScale(0, RoundingMode.DOWN);
    }

    /** 口座の総資産（現金 + 各銘柄の保有額）を返す。 */
    public BigDecimal total() {
        BigDecimal total = cash;
        for (Stock s : stocks) {
            total = total.add(s.amountJpy());
        }
        return total;
    }

    /** 新規注文額を、最適ポートフォリオに沿って配分した口座を生成する。 */
    public static Account openAccount(BigDecimal amount, Portfolio portfolio) {
        BigDecimal cashFromRate = floor0(amount.multiply(AppConstants.CASH_RATE));
        BigDecimal investable = amount.subtract(cashFromRate);
        List<Stock> stocks = new ArrayList<>();
        BigDecimal usedForStocks = BigDecimal.ZERO;
        for (PortfolioItem item : portfolio.items()) {
            BigDecimal amt = floor0(investable.multiply(item.rate()));
            stocks.add(new Stock(item.symbol(), amt));
            usedForStocks = usedForStocks.add(amt);
        }
        BigDecimal residual = investable.subtract(usedForStocks);
        return new Account(cashFromRate.add(residual), stocks);
    }

    /** 追加注文額を口座へ反映する。最適ポートフォリオの目標額を下回らない範囲で
     * 既存の保有額を維持し、ポートフォリオ外の銘柄はそのまま保持する。 */
    public Account addFunds(BigDecimal amount, Portfolio portfolio) {
        BigDecimal totalAfter = this.total().add(amount);
        BigDecimal targetCash = floor0(totalAfter.multiply(AppConstants.CASH_RATE));
        BigDecimal investable = totalAfter.subtract(targetCash);

        Map<StockSymbol, BigDecimal> currentAmount = new HashMap<>();
        for (Stock s : stocks) {
            currentAmount.put(s.symbol(), s.amountJpy());
        }

        Set<StockSymbol> portfolioSymbols = new HashSet<>();
        for (PortfolioItem item : portfolio.items()) {
            portfolioSymbols.add(item.symbol());
        }

        List<Stock> newPortfolioStocks = new ArrayList<>();
        for (PortfolioItem item : portfolio.items()) {
            BigDecimal target = floor0(investable.multiply(item.rate()));
            BigDecimal current = currentAmount.getOrDefault(item.symbol(), BigDecimal.ZERO);
            BigDecimal finalAmt = current.compareTo(target) > 0 ? current : target;
            newPortfolioStocks.add(new Stock(item.symbol(), finalAmt));
        }

        List<Stock> preservedStocks = new ArrayList<>();
        for (Stock s : stocks) {
            if (!portfolioSymbols.contains(s.symbol())) {
                preservedStocks.add(s);
            }
        }

        List<Stock> allStocks = new ArrayList<>();
        allStocks.addAll(newPortfolioStocks);
        allStocks.addAll(preservedStocks);

        BigDecimal finalAmount = BigDecimal.ZERO;
        for (Stock s : allStocks) {
            finalAmount = finalAmount.add(s.amountJpy());
        }
        BigDecimal finalCash = totalAfter.subtract(finalAmount);
        return new Account(finalCash, allStocks);
    }

    /** 保有資産を最適ポートフォリオの比率に近づける。 */
    public Account rebalance(Portfolio portfolio) {
        // XXX this implementation might not be correct
        BigDecimal investable = this.total();
        List<Stock> newStocks = new ArrayList<>();
        BigDecimal usedForStocks = BigDecimal.ZERO;
        for (PortfolioItem item : portfolio.items()) {
            BigDecimal amt = floor0(investable.multiply(item.rate()));
            newStocks.add(new Stock(item.symbol(), amt));
            usedForStocks = usedForStocks.add(amt);
        }
        BigDecimal finalCash = investable.subtract(usedForStocks);
        return new Account(finalCash, newStocks);
    }
}
