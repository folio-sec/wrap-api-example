package folio.codinginterview.domain;

import java.math.BigDecimal;
import java.util.List;

public record Account(BigDecimal cash, List<Stock> stocks) {
    public Account {
        stocks = List.copyOf(stocks);
    }
}
