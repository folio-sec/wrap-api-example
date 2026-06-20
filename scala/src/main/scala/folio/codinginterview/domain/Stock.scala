package folio.codinginterview.domain

import scala.math.BigDecimal.RoundingMode

/** 保有銘柄（銘柄と保有額）を表す。 */
final case class Stock(symbol: StockSymbol, amountJpy: BigDecimal)

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

/** 口座を表す。 */
final case class Account(cash: BigDecimal, stocks: Seq[Stock]) {
  import Account.floor0

  /** 口座の総資産（現金 + 各銘柄の保有額）を返す。 */
  def total: BigDecimal = cash + stocks.map(_.amountJpy).sum

  /** 追加注文額を口座へ反映する。最適ポートフォリオの目標額を下回らない範囲で
   *  既存の保有額を維持し、ポートフォリオ外の銘柄はそのまま保持する。 */
  def addFunds(amount: BigDecimal, portfolio: Portfolio): Account = {
    val totalAfter = total + amount
    val targetCash = floor0(totalAfter * AppConstants.cashRate)
    val investable = totalAfter - targetCash
    val currentAmount: Map[StockSymbol, BigDecimal] =
      stocks.map(e => e.symbol -> e.amountJpy).toMap

    val portfolioSymbols = portfolio.items.map(_.symbol).toSet
    val newPortfolioStocks = portfolio.items.map { item =>
      val target = floor0(investable * item.rate)
      val current = currentAmount.getOrElse(item.symbol, BigDecimal(0))
      val finalAmt = if (current > target) current else target
      Stock(item.symbol, finalAmt)
    }
    val preservedStocks = stocks.filterNot(e => portfolioSymbols.contains(e.symbol))
    val allStocks = newPortfolioStocks ++ preservedStocks

    val finalAmount = allStocks.map(_.amountJpy).sum
    val finalCash = totalAfter - finalAmount
    Account(cash = finalCash, stocks = allStocks)
  }

  /** 保有資産を最適ポートフォリオの比率に近づける。 */
  def rebalance(portfolio: Portfolio): Account = {
    // XXX this implementation might not be correct
    val investable = total
    var usedForStocks = BigDecimal(0)
    val newStocks = portfolio.items.map { item =>
      val amt = floor0(investable * item.rate)
      usedForStocks += amt
      Stock(item.symbol, amt)
    }
    val finalCash = investable - usedForStocks
    Account(cash = finalCash, stocks = newStocks)
  }
}

object Account {
  /** 円未満を切り捨てる（資産配分はすべて円単位で行う）。 */
  private def floor0(x: BigDecimal): BigDecimal = x.setScale(0, RoundingMode.DOWN)

  /** 新規注文額を、最適ポートフォリオに沿って配分した口座を生成する。 */
  def openAccount(amount: BigDecimal, portfolio: Portfolio): Account = {
    val cashFromRate = floor0(amount * AppConstants.cashRate)
    val investable = amount - cashFromRate
    var usedForStocks = BigDecimal(0)
    val stocks = portfolio.items.map { item =>
      val amt = floor0(investable * item.rate)
      usedForStocks += amt
      Stock(item.symbol, amt)
    }
    val residual = investable - usedForStocks
    Account(cash = cashFromRate + residual, stocks = stocks)
  }
}
