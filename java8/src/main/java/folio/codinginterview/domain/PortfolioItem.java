package folio.codinginterview.domain;

import java.math.BigDecimal;

/** ポートフォリオの銘柄ごとの構成比率を表す。 */
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
