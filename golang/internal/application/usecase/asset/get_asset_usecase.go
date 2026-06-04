package asset

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/application/service"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

var ErrUserNotFound = errors.New("user not found")

type GetAssetStockOutput struct {
	Symbol           domain.StockSymbol
	EvaluationAmount decimal.Decimal
}

type GetAssetUsecaseInput struct {
	UserId domain.UserId
}

type GetAssetUsecaseOutput struct {
	CashAmount decimal.Decimal
	Stocks     []GetAssetStockOutput
}

type GetAssetUsecase struct {
	accountRepo       repository.AccountRepository
	marketPriceRepo repository.MarketPriceRepository
}

func NewGetAssetUsecase(accountRepo repository.AccountRepository, marketPriceRepo repository.MarketPriceRepository) *GetAssetUsecase {
	return &GetAssetUsecase{accountRepo: accountRepo, marketPriceRepo: marketPriceRepo}
}

func (u *GetAssetUsecase) Run(input GetAssetUsecaseInput) (GetAssetUsecaseOutput, error) {
	account, err := u.accountRepo.Find(input.UserId)
	if err != nil {
		return GetAssetUsecaseOutput{}, err
	}
	if account == nil {
		return GetAssetUsecaseOutput{}, ErrUserNotFound
	}

	prices, err := u.marketPriceRepo.All()
	if err != nil {
		return GetAssetUsecaseOutput{}, err
	}

	stocks := make([]GetAssetStockOutput, 0, len(account.Stocks))
	for _, s := range account.Stocks {
		evalAmount, err := service.EvaluateStock(s, prices)
		if err != nil {
			return GetAssetUsecaseOutput{}, err
		}
		stocks = append(stocks, GetAssetStockOutput{Symbol: s.Symbol, EvaluationAmount: evalAmount})
	}

	return GetAssetUsecaseOutput{CashAmount: account.Cash, Stocks: stocks}, nil
}
