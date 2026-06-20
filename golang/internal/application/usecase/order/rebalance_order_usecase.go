package order

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"
)

var ErrRebalanceUserNotFound = errors.New("user has no live account")

type RebalanceOrderUsecaseInput struct {
	UserId domain.UserId
}

type RebalanceOrderUsecase struct {
	accountRepo   repository.AccountRepository
	portfolioRepo repository.PortfolioRepository
}

func NewRebalanceOrderUsecase(
	accountRepo repository.AccountRepository,
	portfolioRepo repository.PortfolioRepository,
) *RebalanceOrderUsecase {
	return &RebalanceOrderUsecase{
		accountRepo:   accountRepo,
		portfolioRepo: portfolioRepo,
	}
}

func (u *RebalanceOrderUsecase) Run(input RebalanceOrderUsecaseInput) error {
	account, err := u.accountRepo.Find(input.UserId)
	if err != nil {
		return err
	}
	if account == nil {
		return ErrRebalanceUserNotFound
	}

	portfolio, err := u.portfolioRepo.Get()
	if err != nil {
		return err
	}

	updated := account.Rebalance(portfolio)

	return u.accountRepo.Upsert(input.UserId, updated)
}
