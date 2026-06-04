package service

import (
	"fmt"

	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

func floor2(x decimal.Decimal) decimal.Decimal {
	return x.Truncate(2)
}

func floor0(x decimal.Decimal) decimal.Decimal {
	return x.Truncate(0)
}

func priceOf(prices map[domain.StockSymbol]decimal.Decimal, symbol domain.StockSymbol) (decimal.Decimal, error) {
	p, ok := prices[symbol]
	if !ok {
		return decimal.Zero, fmt.Errorf("price not found for symbol: %s", symbol)
	}
	return p, nil
}

func AllocateNew(amount decimal.Decimal, portfolio domain.Portfolio, prices map[domain.StockSymbol]decimal.Decimal) (domain.Account, error) {
	cashFromRate := floor0(amount.Mul(domain.CashRate))
	investable := amount.Sub(cashFromRate)

	stocks := make([]domain.Stock, 0, len(portfolio.Items))
	for _, item := range portfolio.Items {
		price, err := priceOf(prices, item.Symbol)
		if err != nil {
			return domain.Account{}, err
		}
		qty := floor2(investable.Mul(item.Rate).Div(price))
		stocks = append(stocks, domain.Stock{Symbol: item.Symbol, Qty: qty})
	}

	usedForStocks := decimal.Zero
	for _, s := range stocks {
		price, err := priceOf(prices, s.Symbol)
		if err != nil {
			return domain.Account{}, err
		}
		usedForStocks = usedForStocks.Add(s.Qty.Mul(price))
	}
	residual := investable.Sub(usedForStocks)

	return domain.Account{Cash: cashFromRate.Add(residual), Stocks: stocks}, nil
}

func AllocateAdditional(account domain.Account, amount decimal.Decimal, portfolio domain.Portfolio, prices map[domain.StockSymbol]decimal.Decimal) (domain.Account, error) {
	totalAfterVal, err := TotalValuation(account, prices)
	if err != nil {
		return domain.Account{}, err
	}
	totalAfter := totalAfterVal.Add(amount)
	targetCash := floor0(totalAfter.Mul(domain.CashRate))
	investable := totalAfter.Sub(targetCash)

	currentQty := make(map[domain.StockSymbol]decimal.Decimal)
	for _, s := range account.Stocks {
		currentQty[s.Symbol] = s.Qty
	}

	portfolioSymbols := make(map[domain.StockSymbol]struct{})
	for _, item := range portfolio.Items {
		portfolioSymbols[item.Symbol] = struct{}{}
	}

	newPortfolioStocks := make([]domain.Stock, 0, len(portfolio.Items))
	for _, item := range portfolio.Items {
		price, err := priceOf(prices, item.Symbol)
		if err != nil {
			return domain.Account{}, err
		}
		targetQty := floor2(investable.Mul(item.Rate).Div(price))
		current := currentQty[item.Symbol]
		finalQty := targetQty
		if current.GreaterThan(targetQty) {
			finalQty = current
		}
		newPortfolioStocks = append(newPortfolioStocks, domain.Stock{Symbol: item.Symbol, Qty: finalQty})
	}

	preservedStocks := make([]domain.Stock, 0)
	for _, s := range account.Stocks {
		if _, inPortfolio := portfolioSymbols[s.Symbol]; !inPortfolio {
			preservedStocks = append(preservedStocks, s)
		}
	}

	allStocks := append(newPortfolioStocks, preservedStocks...)

	finalValuation := decimal.Zero
	for _, s := range allStocks {
		price, err := priceOf(prices, s.Symbol)
		if err != nil {
			return domain.Account{}, err
		}
		finalValuation = finalValuation.Add(s.Qty.Mul(price))
	}
	finalCash := totalAfter.Sub(finalValuation)

	return domain.Account{Cash: finalCash, Stocks: allStocks}, nil
}

func Rebalance(account domain.Account, portfolio domain.Portfolio, prices map[domain.StockSymbol]decimal.Decimal) (domain.Account, error) {
	// XXX this implementation might not be correct
	investable, err := TotalValuation(account, prices)
	if err != nil {
		return domain.Account{}, err
	}

	newStocks := make([]domain.Stock, 0, len(portfolio.Items))
	for _, item := range portfolio.Items {
		price, err := priceOf(prices, item.Symbol)
		if err != nil {
			return domain.Account{}, err
		}
		qty := floor2(investable.Mul(item.Rate).Div(price))
		newStocks = append(newStocks, domain.Stock{Symbol: item.Symbol, Qty: qty})
	}

	finalValuation := decimal.Zero
	for _, s := range newStocks {
		price, err := priceOf(prices, s.Symbol)
		if err != nil {
			return domain.Account{}, err
		}
		finalValuation = finalValuation.Add(s.Qty.Mul(price))
	}
	finalCash := investable.Sub(finalValuation)

	return domain.Account{Cash: finalCash, Stocks: newStocks}, nil
}
