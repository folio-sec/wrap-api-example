package presentation

import (
	"fmt"

	marketprice "folio/codinginterview/internal/application/usecase/market_price"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

type MarketPriceItemDto struct {
	Symbol      string
	MarketPrice string
}

type UpdateMarketPriceRequest struct {
	MarketPrices []MarketPriceItemDto
}

type MarketPriceController struct {
	updateMarketPriceUsecase *marketprice.UpdateMarketPriceUsecase
}

func NewMarketPriceController(updateMarketPriceUsecase *marketprice.UpdateMarketPriceUsecase) *MarketPriceController {
	return &MarketPriceController{updateMarketPriceUsecase: updateMarketPriceUsecase}
}

func (c *MarketPriceController) UpdateMarketPrice(req UpdateMarketPriceRequest) error {
	items := make([]marketprice.UpdateMarketPriceItemInput, 0, len(req.MarketPrices))
	for _, dto := range req.MarketPrices {
		sym, err := domain.StockSymbolFromString(dto.Symbol)
		if err != nil {
			return newBadRequest(fmt.Sprintf("unknown symbol: %s", dto.Symbol))
		}
		price, err := decimal.NewFromString(dto.MarketPrice)
		if err != nil {
			return newBadRequest(fmt.Sprintf("invalid market_price: %s", dto.MarketPrice))
		}
		items = append(items, marketprice.UpdateMarketPriceItemInput{Symbol: sym, MarketPrice: price})
	}

	return c.updateMarketPriceUsecase.Run(marketprice.UpdateMarketPriceUsecaseInput{Items: items})
}
