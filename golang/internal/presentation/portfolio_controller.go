package presentation

import (
	"fmt"

	"folio/codinginterview/internal/application/usecase/portfolio"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

type PortfolioItemDto struct {
	Symbol string
	Rate   string
}

type GetOptimalPortfolioResponse struct {
	Portfolios []PortfolioItemDto
}

type UpdateOptimalPortfolioRequest struct {
	Portfolios []PortfolioItemDto
}

type PortfolioController struct {
	getLatestPortfolioUsecase *portfolio.GetLatestPortfolioUsecase
	updatePortfolioUsecase    *portfolio.UpdatePortfolioUsecase
}

func NewPortfolioController(
	getLatestPortfolioUsecase *portfolio.GetLatestPortfolioUsecase,
	updatePortfolioUsecase *portfolio.UpdatePortfolioUsecase,
) *PortfolioController {
	return &PortfolioController{
		getLatestPortfolioUsecase: getLatestPortfolioUsecase,
		updatePortfolioUsecase:    updatePortfolioUsecase,
	}
}

func (c *PortfolioController) GetOptimalPortfolio() (GetOptimalPortfolioResponse, error) {
	out, err := c.getLatestPortfolioUsecase.Run()
	if err != nil {
		return GetOptimalPortfolioResponse{}, err
	}
	portfolios := make([]PortfolioItemDto, 0, len(out.Items))
	for _, item := range out.Items {
		portfolios = append(portfolios, PortfolioItemDto{
			Symbol: string(item.Symbol),
			Rate:   item.Rate.String(),
		})
	}
	return GetOptimalPortfolioResponse{Portfolios: portfolios}, nil
}

func (c *PortfolioController) UpdateOptimalPortfolio(req UpdateOptimalPortfolioRequest) error {
	items := make([]portfolio.UpdatePortfolioItemInput, 0, len(req.Portfolios))
	for _, dto := range req.Portfolios {
		sym, err := domain.StockSymbolFromString(dto.Symbol)
		if err != nil {
			return newBadRequest(fmt.Sprintf("unknown symbol: %s", dto.Symbol))
		}
		rate, err := decimal.NewFromString(dto.Rate)
		if err != nil {
			return newBadRequest(fmt.Sprintf("invalid rate: %s", dto.Rate))
		}
		items = append(items, portfolio.UpdatePortfolioItemInput{Symbol: sym, Rate: rate})
	}

	err := c.updatePortfolioUsecase.Run(portfolio.UpdatePortfolioUsecaseInput{Items: items})
	if err != nil {
		if invalidErr, ok := portfolio.AsInvalidPortfolioError(err); ok {
			return newBadRequest(invalidErr.Reason)
		}
		return err
	}
	return nil
}
