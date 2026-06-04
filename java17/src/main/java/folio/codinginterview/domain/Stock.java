package folio.codinginterview.domain;

import java.math.BigDecimal;

public record Stock(StockSymbol symbol, BigDecimal qty) {
}
