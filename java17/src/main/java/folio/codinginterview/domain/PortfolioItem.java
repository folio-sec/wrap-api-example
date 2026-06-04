package folio.codinginterview.domain;

import java.math.BigDecimal;

public record PortfolioItem(StockSymbol symbol, BigDecimal rate) {
}
