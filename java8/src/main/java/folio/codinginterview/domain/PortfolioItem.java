package folio.codinginterview.domain;

import java.math.BigDecimal;

public final class PortfolioItem {
    private final StockSymbol symbol;
    private final BigDecimal rate;

    public PortfolioItem(StockSymbol symbol, BigDecimal rate) {
        this.symbol = symbol;
        this.rate = rate;
    }

    public StockSymbol symbol() { return symbol; }

    public BigDecimal rate() { return rate; }
}
