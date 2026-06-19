package folio.codinginterview.domain

/** アプリケーション定数。 */
object AppConstants {
  val cashRate: BigDecimal = BigDecimal("0.05")

  val minOperationAmount: BigDecimal = BigDecimal(10000)

  val supportedSymbols: Seq[StockSymbol] = Seq(StockSymbol.Toyopa, StockSymbol.Somy)

  val initialPortfolio: Portfolio = Portfolio(
    Seq(
      PortfolioItem(StockSymbol.Toyopa, BigDecimal("0.40")),
      PortfolioItem(StockSymbol.Somy, BigDecimal("0.60"))
    )
  )
}
