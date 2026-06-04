package domain

import "github.com/shopspring/decimal"

var (
	CashRate           = decimal.RequireFromString("0.05")
	MinOperationAmount = decimal.NewFromInt(10000)
	SupportedSymbols   = []StockSymbol{Toyopa, Somy}

	InitialPrices = map[StockSymbol]decimal.Decimal{
		Toyopa: decimal.RequireFromString("4.2135"),
		Somy:   decimal.RequireFromString("1.2345"),
	}
)

func MustInitialPortfolio() Portfolio {
	p, err := NewPortfolio([]PortfolioItem{
		{Symbol: Toyopa, Rate: decimal.RequireFromString("0.40")},
		{Symbol: Somy, Rate: decimal.RequireFromString("0.60")},
	})
	if err != nil {
		panic(err)
	}
	return p
}
