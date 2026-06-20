package order

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

var (
	ErrAdditionalBuyUserNotFound   = errors.New("user has no live account")
	ErrAdditionalBuyAmountTooSmall = errors.New("amount is too small")
)

type AdditionalBuyOrderUsecaseInput struct {
	UserId domain.UserId
	Amount decimal.Decimal
}

type AdditionalBuyOrderUsecase struct {
	accountRepo   repository.AccountRepository
	portfolioRepo repository.PortfolioRepository
}

func NewAdditionalBuyOrderUsecase(
	accountRepo repository.AccountRepository,
	portfolioRepo repository.PortfolioRepository,
) *AdditionalBuyOrderUsecase {
	return &AdditionalBuyOrderUsecase{
		accountRepo:   accountRepo,
		portfolioRepo: portfolioRepo,
	}
}

func (u *AdditionalBuyOrderUsecase) Run(input AdditionalBuyOrderUsecaseInput) error {
	if input.Amount.LessThan(domain.MinOperationAmount) {
		return ErrAdditionalBuyAmountTooSmall
	}

	account, err := u.accountRepo.Find(input.UserId)
	if err != nil {
		return err
	}
	if account == nil {
		return ErrAdditionalBuyUserNotFound
	}

	portfolio, err := u.portfolioRepo.Get()
	if err != nil {
		return err
	}

	updated := account.AddFunds(input.Amount, portfolio)

	return u.accountRepo.Upsert(input.UserId, updated)
}
