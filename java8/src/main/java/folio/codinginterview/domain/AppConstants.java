package folio.codinginterview.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class AppConstants {
    private AppConstants() {}

    public static final BigDecimal CASH_RATE = new BigDecimal("0.05");

    public static final BigDecimal MIN_OPERATION_AMOUNT = new BigDecimal(10000);

    public static final List<StockSymbol> SUPPORTED_SYMBOLS =
            Collections.unmodifiableList(Arrays.asList(StockSymbol.Toyopa, StockSymbol.Somy));

    public static final Portfolio INITIAL_PORTFOLIO = new Portfolio(Arrays.asList(
            new PortfolioItem(StockSymbol.Toyopa, new BigDecimal("0.40")),
            new PortfolioItem(StockSymbol.Somy, new BigDecimal("0.60"))
    ));
}
