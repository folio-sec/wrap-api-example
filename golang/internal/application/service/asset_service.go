package service

import (
	"fmt"

	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

func EvaluateStock(stock domain.Stock, prices map[domain.StockSymbol]decimal.Decimal) (decimal.Decimal, error) {
	price, ok := prices[stock.Symbol]
	if !ok {
		return decimal.Zero, fmt.Errorf("price not found for symbol: %s", stock.Symbol)
	}
	return stock.Qty.Mul(price), nil
}

func TotalValuation(account domain.Account, prices map[domain.StockSymbol]decimal.Decimal) (decimal.Decimal, error) {
	total := account.Cash
	for _, s := range account.Stocks {
		val, err := EvaluateStock(s, prices)
		if err != nil {
			return decimal.Zero, err
		}
		total = total.Add(val)
	}
	return total, nil
}
