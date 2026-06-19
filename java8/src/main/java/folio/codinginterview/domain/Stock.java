package folio.codinginterview.domain;

import java.math.BigDecimal;

// 銘柄と保有額を表す。
public final class Stock {
    private final StockSymbol symbol;
    private final BigDecimal amountJpy;

    public Stock(StockSymbol symbol, BigDecimal amountJpy) {
        this.symbol = symbol;
        this.amountJpy = amountJpy;
    }

    public StockSymbol symbol() { return symbol; }

    public BigDecimal amountJpy() { return amountJpy; }
}
