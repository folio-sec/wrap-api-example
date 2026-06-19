package asset

import (
	"errors"

	"folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

var ErrUserNotFound = errors.New("user not found")

type GetAssetStockOutput struct {
	Symbol    domain.StockSymbol
	AmountJpy decimal.Decimal
}

type GetAssetUsecaseInput struct {
	UserId domain.UserId
}

type GetAssetUsecaseOutput struct {
	CashAmount decimal.Decimal
	Stocks     []GetAssetStockOutput
}

type GetAssetUsecase struct {
	accountRepo repository.AccountRepository
}

func NewGetAssetUsecase(accountRepo repository.AccountRepository) *GetAssetUsecase {
	return &GetAssetUsecase{accountRepo: accountRepo}
}

func (u *GetAssetUsecase) Run(input GetAssetUsecaseInput) (GetAssetUsecaseOutput, error) {
	account, err := u.accountRepo.Find(input.UserId)
	if err != nil {
		return GetAssetUsecaseOutput{}, err
	}
	if account == nil {
		return GetAssetUsecaseOutput{}, ErrUserNotFound
	}

	stocks := make([]GetAssetStockOutput, 0, len(account.Stocks))
	for _, s := range account.Stocks {
		stocks = append(stocks, GetAssetStockOutput{Symbol: s.Symbol, AmountJpy: s.AmountJpy})
	}

	return GetAssetUsecaseOutput{CashAmount: account.Cash, Stocks: stocks}, nil
}
