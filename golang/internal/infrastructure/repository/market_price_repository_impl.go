package repository

import (
	"sync"

	apprepository "folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

type MarketPriceRepositoryImpl struct {
	mu     sync.RWMutex
	prices map[domain.StockSymbol]decimal.Decimal
}

func NewMarketPriceRepositoryImpl() apprepository.MarketPriceRepository {
	prices := make(map[domain.StockSymbol]decimal.Decimal)
	for k, v := range domain.InitialPrices {
		prices[k] = v
	}
	return &MarketPriceRepositoryImpl{prices: prices}
}

func (r *MarketPriceRepositoryImpl) All() (map[domain.StockSymbol]decimal.Decimal, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	result := make(map[domain.StockSymbol]decimal.Decimal, len(r.prices))
	for k, v := range r.prices {
		result[k] = v
	}
	return result, nil
}

func (r *MarketPriceRepositoryImpl) Update(prices map[domain.StockSymbol]decimal.Decimal) error {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.prices = prices
	return nil
}
