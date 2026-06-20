package domain

import "github.com/shopspring/decimal"

// Account は口座を表す。
type Account struct {
	Cash   decimal.Decimal
	Stocks []Stock
}

// floor0 は円未満を切り捨てる（資産配分はすべて円単位で行う）。
func floor0(x decimal.Decimal) decimal.Decimal {
	return x.Truncate(0)
}

// Total は口座の総資産（現金 + 各銘柄の保有額）を返す。
func (a Account) Total() decimal.Decimal {
	total := a.Cash
	for _, s := range a.Stocks {
		total = total.Add(s.AmountJpy)
	}
	return total
}

// OpenAccount は新規注文額を、最適ポートフォリオに沿って配分した口座を生成する。
func OpenAccount(amount decimal.Decimal, portfolio Portfolio) Account {
	cashFromRate := floor0(amount.Mul(CashRate))
	investable := amount.Sub(cashFromRate)

	stocks := make([]Stock, 0, len(portfolio.Items))
	usedForStocks := decimal.Zero
	for _, item := range portfolio.Items {
		amt := floor0(investable.Mul(item.Rate))
		stocks = append(stocks, Stock{Symbol: item.Symbol, AmountJpy: amt})
		usedForStocks = usedForStocks.Add(amt)
	}

	residual := investable.Sub(usedForStocks)

	return Account{Cash: cashFromRate.Add(residual), Stocks: stocks}
}

// AddFunds は追加注文額を口座へ反映する。最適ポートフォリオの目標額を下回らない範囲で
// 既存の保有額を維持し、ポートフォリオ外の銘柄はそのまま保持する。
func (a Account) AddFunds(amount decimal.Decimal, portfolio Portfolio) Account {
	totalAfter := a.Total().Add(amount)
	targetCash := floor0(totalAfter.Mul(CashRate))
	investable := totalAfter.Sub(targetCash)

	currentAmount := make(map[StockSymbol]decimal.Decimal)
	for _, s := range a.Stocks {
		currentAmount[s.Symbol] = s.AmountJpy
	}

	portfolioSymbols := make(map[StockSymbol]struct{})
	for _, item := range portfolio.Items {
		portfolioSymbols[item.Symbol] = struct{}{}
	}

	newPortfolioStocks := make([]Stock, 0, len(portfolio.Items))
	for _, item := range portfolio.Items {
		target := floor0(investable.Mul(item.Rate))
		current := currentAmount[item.Symbol]
		final := target
		if current.GreaterThan(target) {
			final = current
		}
		newPortfolioStocks = append(newPortfolioStocks, Stock{Symbol: item.Symbol, AmountJpy: final})
	}

	preservedStocks := make([]Stock, 0)
	for _, s := range a.Stocks {
		if _, inPortfolio := portfolioSymbols[s.Symbol]; !inPortfolio {
			preservedStocks = append(preservedStocks, s)
		}
	}

	allStocks := append(newPortfolioStocks, preservedStocks...)

	finalAmount := decimal.Zero
	for _, s := range allStocks {
		finalAmount = finalAmount.Add(s.AmountJpy)
	}
	finalCash := totalAfter.Sub(finalAmount)

	return Account{Cash: finalCash, Stocks: allStocks}
}

// Rebalance は保有資産を最適ポートフォリオの比率に近づける。
func (a Account) Rebalance(portfolio Portfolio) Account {
	// XXX this implementation might not be correct
	investable := a.Total()

	newStocks := make([]Stock, 0, len(portfolio.Items))
	usedForStocks := decimal.Zero
	for _, item := range portfolio.Items {
		amt := floor0(investable.Mul(item.Rate))
		newStocks = append(newStocks, Stock{Symbol: item.Symbol, AmountJpy: amt})
		usedForStocks = usedForStocks.Add(amt)
	}

	finalCash := investable.Sub(usedForStocks)

	return Account{Cash: finalCash, Stocks: newStocks}
}
