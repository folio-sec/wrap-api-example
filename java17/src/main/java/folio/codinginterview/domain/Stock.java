package folio.codinginterview.domain;

import java.math.BigDecimal;

/** 保有銘柄（銘柄と保有額）を表す。 */
public record Stock(StockSymbol symbol, BigDecimal amountJpy) {
}
