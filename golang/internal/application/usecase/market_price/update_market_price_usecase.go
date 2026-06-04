package marketprice

import (
	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

type UpdateMarketPriceItemInput struct {
	Symbol      domain.StockSymbol
	MarketPrice decimal.Decimal
}

type UpdateMarketPriceUsecaseInput struct {
	Items []UpdateMarketPriceItemInput
}

type UpdateMarketPriceUsecase struct {
	marketPriceRepo repository.MarketPriceRepository
}

func NewUpdateMarketPriceUsecase(marketPriceRepo repository.MarketPriceRepository) *UpdateMarketPriceUsecase {
	return &UpdateMarketPriceUsecase{marketPriceRepo: marketPriceRepo}
}

func (u *UpdateMarketPriceUsecase) Run(input UpdateMarketPriceUsecaseInput) error {
	prices := make(map[domain.StockSymbol]decimal.Decimal, len(input.Items))
	for _, item := range input.Items {
		prices[item.Symbol] = item.MarketPrice
	}
	return u.marketPriceRepo.Update(prices)
}
