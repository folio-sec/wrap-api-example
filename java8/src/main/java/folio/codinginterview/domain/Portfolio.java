package folio.codinginterview.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Portfolio {
    private final List<PortfolioItem> items;

    public Portfolio(List<PortfolioItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("portfolio must have at least one item");
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (PortfolioItem i : items) {
            sum = sum.add(i.rate());
        }
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("portfolio rates must sum to 1, got " + sum);
        }
        Set<StockSymbol> symbols = new HashSet<>();
        for (PortfolioItem i : items) {
            symbols.add(i.symbol());
        }
        if (symbols.size() != items.size()) {
            throw new IllegalArgumentException("portfolio must not have duplicate symbols");
        }
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public List<PortfolioItem> items() { return items; }
}
