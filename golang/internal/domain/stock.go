package domain

import (
	"errors"
	"fmt"

	"github.com/shopspring/decimal"
)

type Stock struct {
	Symbol StockSymbol
	Qty    decimal.Decimal
}

type PortfolioItem struct {
	Symbol StockSymbol
	Rate   decimal.Decimal
}

type Portfolio struct {
	Items []PortfolioItem
}

func NewPortfolio(items []PortfolioItem) (Portfolio, error) {
	if len(items) == 0 {
		return Portfolio{}, errors.New("portfolio must have at least one item")
	}
	sum := decimal.Zero
	for _, item := range items {
		sum = sum.Add(item.Rate)
	}
	if !sum.Equal(decimal.NewFromInt(1)) {
		return Portfolio{}, fmt.Errorf("portfolio rates must sum to 1, got %s", sum.String())
	}
	seen := make(map[StockSymbol]struct{})
	for _, item := range items {
		if _, ok := seen[item.Symbol]; ok {
			return Portfolio{}, errors.New("portfolio must not have duplicate symbols")
		}
		seen[item.Symbol] = struct{}{}
	}
	return Portfolio{Items: items}, nil
}

type Account struct {
	Cash   decimal.Decimal
	Stocks []Stock
}
