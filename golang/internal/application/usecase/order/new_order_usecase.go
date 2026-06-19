package order

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

var (
	ErrNewOrderUserAlreadyExists = errors.New("user already has account")
	ErrNewOrderAmountTooSmall    = errors.New("amount is too small")
)

type NewOrderUsecaseInput struct {
	UserId domain.UserId
	Amount decimal.Decimal
}

type NewOrderUsecase struct {
	accountRepo   repository.AccountRepository
	portfolioRepo repository.PortfolioRepository
}

func NewNewOrderUsecase(
	accountRepo repository.AccountRepository,
	portfolioRepo repository.PortfolioRepository,
) *NewOrderUsecase {
	return &NewOrderUsecase{
		accountRepo:   accountRepo,
		portfolioRepo: portfolioRepo,
	}
}

func (u *NewOrderUsecase) Run(input NewOrderUsecaseInput) error {
	if input.Amount.LessThan(domain.MinOperationAmount) {
		return ErrNewOrderAmountTooSmall
	}

	exists, err := u.accountRepo.Exists(input.UserId)
	if err != nil {
		return err
	}
	if exists {
		return ErrNewOrderUserAlreadyExists
	}

	portfolio, err := u.portfolioRepo.Get()
	if err != nil {
		return err
	}

	account := domain.OpenAccount(input.Amount, portfolio)

	return u.accountRepo.Upsert(input.UserId, account)
}
