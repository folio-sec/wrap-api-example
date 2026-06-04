package folio.codinginterview.application.service

import folio.codinginterview.domain.Account
import folio.codinginterview.domain.Stock
import folio.codinginterview.domain.StockSymbol

object AssetService {
  def evaluateStock(stock: Stock, prices: Map[StockSymbol, BigDecimal]): BigDecimal = {
    val price = prices.getOrElse(
      stock.symbol,
      throw new IllegalStateException(s"missing price for ${stock.symbol}")
    )
    stock.qty * price
  }

  def totalValuation(account: Account, prices: Map[StockSymbol, BigDecimal]): BigDecimal = {
    account.stocks.map(e => evaluateStock(e, prices)).sum + account.cash
  }
}
