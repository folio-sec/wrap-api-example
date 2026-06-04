package portfolio

import (
	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

type GetLatestPortfolioItemOutput struct {
	Symbol domain.StockSymbol
	Rate   decimal.Decimal
}

type GetLatestPortfolioUsecaseOutput struct {
	Items []GetLatestPortfolioItemOutput
}

type GetLatestPortfolioUsecase struct {
	portfolioRepo repository.PortfolioRepository
}

func NewGetLatestPortfolioUsecase(portfolioRepo repository.PortfolioRepository) *GetLatestPortfolioUsecase {
	return &GetLatestPortfolioUsecase{portfolioRepo: portfolioRepo}
}

func (u *GetLatestPortfolioUsecase) Run() (GetLatestPortfolioUsecaseOutput, error) {
	p, err := u.portfolioRepo.Get()
	if err != nil {
		return GetLatestPortfolioUsecaseOutput{}, err
	}
	items := make([]GetLatestPortfolioItemOutput, 0, len(p.Items))
	for _, item := range p.Items {
		items = append(items, GetLatestPortfolioItemOutput{Symbol: item.Symbol, Rate: item.Rate})
	}
	return GetLatestPortfolioUsecaseOutput{Items: items}, nil
}
