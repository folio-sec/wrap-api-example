package portfolio

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

type InvalidPortfolioError struct {
	Reason string
}

func (e *InvalidPortfolioError) Error() string {
	return e.Reason
}

type UpdatePortfolioItemInput struct {
	Symbol domain.StockSymbol
	Rate   decimal.Decimal
}

type UpdatePortfolioUsecaseInput struct {
	Items []UpdatePortfolioItemInput
}

type UpdatePortfolioUsecase struct {
	portfolioRepo repository.PortfolioRepository
}

func NewUpdatePortfolioUsecase(portfolioRepo repository.PortfolioRepository) *UpdatePortfolioUsecase {
	return &UpdatePortfolioUsecase{portfolioRepo: portfolioRepo}
}

func (u *UpdatePortfolioUsecase) Run(input UpdatePortfolioUsecaseInput) error {
	items := make([]domain.PortfolioItem, 0, len(input.Items))
	for _, i := range input.Items {
		items = append(items, domain.PortfolioItem{Symbol: i.Symbol, Rate: i.Rate})
	}
	p, err := domain.NewPortfolio(items)
	if err != nil {
		return &InvalidPortfolioError{Reason: err.Error()}
	}
	return u.portfolioRepo.Update(p)
}

func AsInvalidPortfolioError(err error) (*InvalidPortfolioError, bool) {
	var e *InvalidPortfolioError
	if errors.As(err, &e) {
		return e, true
	}
	return nil, false
}
