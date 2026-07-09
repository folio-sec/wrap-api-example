package folio.codinginterview.domain

/** ポートフォリオの銘柄ごとの構成比率を表す。 */
final case class PortfolioItem(symbol: StockSymbol, rate: BigDecimal)

/** 最適ポートフォリオ（銘柄ごとの構成比率）を表す。 */
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
