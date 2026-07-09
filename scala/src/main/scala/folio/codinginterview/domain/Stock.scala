package folio.codinginterview.domain

/** 保有銘柄（銘柄と保有額）を表す。 */
final case class Stock(symbol: StockSymbol, amountJpy: BigDecimal)
