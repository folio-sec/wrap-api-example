package order

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/application/service"
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
	accountRepo       repository.AccountRepository
	portfolioRepo   repository.PortfolioRepository
	marketPriceRepo repository.MarketPriceRepository
}

func NewAdditionalBuyOrderUsecase(
	accountRepo repository.AccountRepository,
	portfolioRepo repository.PortfolioRepository,
	marketPriceRepo repository.MarketPriceRepository,
) *AdditionalBuyOrderUsecase {
	return &AdditionalBuyOrderUsecase{
		accountRepo:       accountRepo,
		portfolioRepo:   portfolioRepo,
		marketPriceRepo: marketPriceRepo,
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

	prices, err := u.marketPriceRepo.All()
	if err != nil {
		return err
	}

	updated, err := service.AllocateAdditional(*account, input.Amount, portfolio, prices)
	if err != nil {
		return err
	}

	return u.accountRepo.Upsert(input.UserId, updated)
}
