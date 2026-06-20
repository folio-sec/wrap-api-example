package presentation

import (
	"errors"

	"folio/codinginterview/internal/application/usecase/order"
)

type NewOrderRequest struct {
	UserId string
	Amount string
}

type AdditionalOrderRequest struct {
	UserId string
	Amount string
}

type RebalanceOrderRequest struct {
	UserId string
}

type OrderController struct {
	newOrderUsecase *order.NewOrderUsecase
	additionalBuyOrderUsecase   *order.AdditionalBuyOrderUsecase
	rebalanceOrderUsecase       *order.RebalanceOrderUsecase
}

func NewOrderController(
	newOrderUsecase *order.NewOrderUsecase,
	additionalBuyOrderUsecase *order.AdditionalBuyOrderUsecase,
	rebalanceOrderUsecase *order.RebalanceOrderUsecase,
) *OrderController {
	return &OrderController{
		newOrderUsecase: newOrderUsecase,
		additionalBuyOrderUsecase:   additionalBuyOrderUsecase,
		rebalanceOrderUsecase:       rebalanceOrderUsecase,
	}
}

func (c *OrderController) NewOrder(req NewOrderRequest) error {
	uid, err := parseUserId(req.UserId)
	if err != nil {
		return err
	}
	amt, err := parseAmount(req.Amount)
	if err != nil {
		return err
	}

	err = c.newOrderUsecase.Run(order.NewOrderUsecaseInput{UserId: uid, Amount: amt})
	if err != nil {
		if errors.Is(err, order.ErrNewOrderUserAlreadyExists) {
			return newBadRequest("user already has account")
		}
		if errors.Is(err, order.ErrNewOrderAmountTooSmall) {
			return newBadRequest("amount is too small")
		}
		return err
	}
	return nil
}

func (c *OrderController) AdditionalOrder(req AdditionalOrderRequest) error {
	uid, err := parseUserId(req.UserId)
	if err != nil {
		return err
	}
	amt, err := parseAmount(req.Amount)
	if err != nil {
		return err
	}

	err = c.additionalBuyOrderUsecase.Run(order.AdditionalBuyOrderUsecaseInput{UserId: uid, Amount: amt})
	if err != nil {
		if errors.Is(err, order.ErrAdditionalBuyUserNotFound) {
			return newBadRequest("user has no live account")
		}
		if errors.Is(err, order.ErrAdditionalBuyAmountTooSmall) {
			return newBadRequest("amount is too small")
		}
		return err
	}
	return nil
}

func (c *OrderController) RebalanceOrder(req RebalanceOrderRequest) error {
	uid, err := parseUserId(req.UserId)
	if err != nil {
		return err
	}

	err = c.rebalanceOrderUsecase.Run(order.RebalanceOrderUsecaseInput{UserId: uid})
	if err != nil {
		if errors.Is(err, order.ErrRebalanceUserNotFound) {
			return newBadRequest("user has no live account")
		}
		return err
	}
	return nil
}
