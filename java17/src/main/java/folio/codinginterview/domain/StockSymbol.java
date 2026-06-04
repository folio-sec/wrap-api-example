package folio.codinginterview.domain;

import java.util.Optional;

public enum StockSymbol {
    Toyopa, Somy;

    public static Optional<StockSymbol> fromString(String s) {
        return switch (s) {
            case "Toyopa" -> Optional.of(Toyopa);
            case "Somy" -> Optional.of(Somy);
            default -> Optional.empty();
        };
    }
}
