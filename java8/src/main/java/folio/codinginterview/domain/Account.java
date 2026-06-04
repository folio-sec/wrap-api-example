package folio.codinginterview.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Account {
    private final BigDecimal cash;
    private final List<Stock> stocks;

    public Account(BigDecimal cash, List<Stock> stocks) {
        this.cash = cash;
        this.stocks = Collections.unmodifiableList(new ArrayList<>(stocks));
    }

    public BigDecimal cash() { return cash; }

    public List<Stock> stocks() { return stocks; }
}
