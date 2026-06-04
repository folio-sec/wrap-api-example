package folio.codinginterview.domain

final case class Stock(symbol: StockSymbol, qty: BigDecimal)

final case class PortfolioItem(symbol: StockSymbol, rate: BigDecimal)

final case class Portfolio(items: Seq[PortfolioItem]) {
  require(items.nonEmpty, "portfolio must have at least one item")
  require(
    items.map(_.rate).sum == BigDecimal(1),
    s"portfolio rates must sum to 1, got ${items.map(_.rate).sum}"
  )
  require(
    items.map(_.symbol).toSet.size == items.size,
    "portfolio must not have duplicate symbols"
  )
}

final case class Account(cash: BigDecimal, stocks: Seq[Stock])
