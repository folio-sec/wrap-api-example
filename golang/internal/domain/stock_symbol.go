package domain

import "fmt"

type StockSymbol string

const (
	Toyopa StockSymbol = "Toyopa"
	Somy   StockSymbol = "Somy"
)

func StockSymbolFromString(s string) (StockSymbol, error) {
	switch s {
	case "Toyopa":
		return Toyopa, nil
	case "Somy":
		return Somy, nil
	default:
		return "", fmt.Errorf("unknown symbol: %s", s)
	}
}
