package presentation

import (
	"fmt"

	"folio/codinginterview/internal/domain"

	"github.com/shopspring/decimal"
)

func parseUserId(s string) (domain.UserId, error) {
	uid, err := domain.NewUserId(s)
	if err != nil {
		return domain.UserId{}, newBadRequest(err.Error())
	}
	return uid, nil
}

func parseAmount(s string) (decimal.Decimal, error) {
	d, err := decimal.NewFromString(s)
	if err != nil {
		return decimal.Zero, newBadRequest(fmt.Sprintf("invalid amount: %s", s))
	}
	return d, nil
}
