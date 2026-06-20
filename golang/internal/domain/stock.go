package domain

import (
	"errors"
	"fmt"

	"github.com/shopspring/decimal"
)

// Stock は保有銘柄（銘柄と保有額）を表す。
type Stock struct {
	Symbol    StockSymbol
	AmountJpy decimal.Decimal
}

type PortfolioItem struct {
	Symbol StockSymbol
	Rate   decimal.Decimal
}

// Portfolio は最適ポートフォリオ（銘柄ごとの構成比率）を表す。
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
