package repository

import (
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

// MarketPriceRepository は市場価格のリポジトリインターフェースです。
type MarketPriceRepository interface {
	All() (map[domain.StockSymbol]decimal.Decimal, error)
	Update(prices map[domain.StockSymbol]decimal.Decimal) error
}
