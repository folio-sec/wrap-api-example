package folio.codinginterview.application.service

import folio.codinginterview.domain.Account
import folio.codinginterview.domain.AppConstants
import folio.codinginterview.domain.Stock
import folio.codinginterview.domain.StockSymbol
import folio.codinginterview.domain.Portfolio
import scala.math.BigDecimal.RoundingMode

object PortfolioService {
  private def floor2(x: BigDecimal): BigDecimal = x.setScale(2, RoundingMode.DOWN)
  private def floor0(x: BigDecimal): BigDecimal = x.setScale(0, RoundingMode.DOWN)
  private def priceOf(prices: Map[StockSymbol, BigDecimal], symbol: StockSymbol): BigDecimal =
    prices.getOrElse(symbol, throw new IllegalStateException(s"missing price for $symbol"))

  /** Allocate a brand-new account given a contribution amount. */
  def allocateNew(
      amount: BigDecimal,
      portfolio: Portfolio,
      prices: Map[StockSymbol, BigDecimal]
  ): Account = {
    val cashFromRate = floor0(amount * AppConstants.cashRate)
    val investable = amount - cashFromRate
    val stocks = portfolio.items.map { item =>
      val price = priceOf(prices, item.symbol)
      val qty = floor2(investable * item.rate / price)
      Stock(item.symbol, qty)
    }
    val usedForStocks = stocks.map(e => e.qty * priceOf(prices, e.symbol)).sum
    val residual = investable - usedForStocks
    Account(cash = cashFromRate + residual, stocks = stocks)
  }

  /** Additional contribution: only buy (no sell). Residual is kept in cash. */
  def allocateAdditional(
      account: Account,
      amount: BigDecimal,
      portfolio: Portfolio,
      prices: Map[StockSymbol, BigDecimal]
  ): Account = {
    val totalAfter = AssetService.totalValuation(account, prices) + amount
    val targetCash = floor0(totalAfter * AppConstants.cashRate)
    val investable = totalAfter - targetCash
    val currentQty: Map[StockSymbol, BigDecimal] =
      account.stocks.map(e => e.symbol -> e.qty).toMap

    val portfolioSymbols = portfolio.items.map(_.symbol).toSet
    val newPortfolioStocks = portfolio.items.map { item =>
      val price = priceOf(prices, item.symbol)
      val targetQty = floor2(investable * item.rate / price)
      val current = currentQty.getOrElse(item.symbol, BigDecimal(0))
      val finalQty = if (targetQty > current) targetQty else current
      Stock(item.symbol, finalQty)
    }
    val preservedStocks = account.stocks.filterNot(e => portfolioSymbols.contains(e.symbol))
    val allStocks = newPortfolioStocks ++ preservedStocks

    val finalValuation = allStocks.map(e => e.qty * priceOf(prices, e.symbol)).sum
    val finalCash = totalAfter - finalValuation
    Account(cash = finalCash, stocks = allStocks)
  }

  /** Rebalance: re-allocate qty per portfolio target (buy and sell). */
  def rebalance(
      account: Account,
      portfolio: Portfolio,
      prices: Map[StockSymbol, BigDecimal]
  ): Account = {
    // XXX this implementation might not be correct
    val investable = AssetService.totalValuation(account, prices)
    val newStocks = portfolio.items.map { item =>
      val price = priceOf(prices, item.symbol)
      val qty = floor2(investable * item.rate / price)
      Stock(item.symbol, qty)
    }
    val finalValuation = newStocks.map(e => e.qty * priceOf(prices, e.symbol)).sum
    val finalCash = investable - finalValuation
    Account(cash = finalCash, stocks = newStocks)
  }
}
