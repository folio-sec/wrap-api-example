package folio.codinginterview.domain;

import java.math.BigDecimal;

/** ポートフォリオの銘柄ごとの構成比率を表す。 */
public record PortfolioItem(StockSymbol symbol, BigDecimal rate) {
}
