package folio.codinginterview.domain

object AppConstants {
  val cashRate: BigDecimal = BigDecimal("0.05")

  val minOperationAmount: BigDecimal = BigDecimal(10000)

  val supportedSymbols: Seq[StockSymbol] = Seq(StockSymbol.Toyopa, StockSymbol.Somy)

  val initialPrices: Map[StockSymbol, BigDecimal] = Map(
    StockSymbol.Toyopa -> BigDecimal("4.2135"),
    StockSymbol.Somy -> BigDecimal("1.2345")
  )

  val initialPortfolio: Portfolio = Portfolio(
    Seq(
      PortfolioItem(StockSymbol.Toyopa, BigDecimal("0.40")),
      PortfolioItem(StockSymbol.Somy, BigDecimal("0.60"))
    )
  )
}
