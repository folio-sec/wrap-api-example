package order

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/application/service"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

var (
	ErrNewContributionUserAlreadyExists = errors.New("user already has account")
	ErrNewContributionAmountTooSmall    = errors.New("amount is too small")
)

type NewContributionOrderUsecaseInput struct {
	UserId domain.UserId
	Amount decimal.Decimal
}

type NewContributionOrderUsecase struct {
	accountRepo       repository.AccountRepository
	portfolioRepo   repository.PortfolioRepository
	marketPriceRepo repository.MarketPriceRepository
}

func NewNewContributionOrderUsecase(
	accountRepo repository.AccountRepository,
	portfolioRepo repository.PortfolioRepository,
	marketPriceRepo repository.MarketPriceRepository,
) *NewContributionOrderUsecase {
	return &NewContributionOrderUsecase{
		accountRepo:       accountRepo,
		portfolioRepo:   portfolioRepo,
		marketPriceRepo: marketPriceRepo,
	}
}

func (u *NewContributionOrderUsecase) Run(input NewContributionOrderUsecaseInput) error {
	if input.Amount.LessThan(domain.MinOperationAmount) {
		return ErrNewContributionAmountTooSmall
	}

	exists, err := u.accountRepo.Exists(input.UserId)
	if err != nil {
		return err
	}
	if exists {
		return ErrNewContributionUserAlreadyExists
	}

	portfolio, err := u.portfolioRepo.Get()
	if err != nil {
		return err
	}

	prices, err := u.marketPriceRepo.All()
	if err != nil {
		return err
	}

	account, err := service.AllocateNew(input.Amount, portfolio, prices)
	if err != nil {
		return err
	}

	return u.accountRepo.Upsert(input.UserId, account)
}
