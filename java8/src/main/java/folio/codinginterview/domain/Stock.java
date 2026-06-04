package folio.codinginterview.domain;

import java.math.BigDecimal;

public final class Stock {
    private final StockSymbol symbol;
    private final BigDecimal qty;

    public Stock(StockSymbol symbol, BigDecimal qty) {
        this.symbol = symbol;
        this.qty = qty;
    }

    public StockSymbol symbol() { return symbol; }

    public BigDecimal qty() { return qty; }
}
