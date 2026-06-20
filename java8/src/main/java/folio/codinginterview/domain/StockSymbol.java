package folio.codinginterview.domain;

import java.util.Optional;

/** 銘柄を表す。 */
public enum StockSymbol {
    Toyopa, Somy;

    public static Optional<StockSymbol> fromString(String s) {
        if ("Toyopa".equals(s)) return Optional.of(Toyopa);
        if ("Somy".equals(s)) return Optional.of(Somy);
        return Optional.empty();
    }
}
