package presentation

import (
	"errors"

	"folio/codinginterview/internal/application/usecase/order"
)

type NewContributionOrderRequest struct {
	UserId string
	Amount string
}

type AdditionalContributionOrderRequest struct {
	UserId string
	Amount string
}

type RebalanceOrderRequest struct {
	UserId string
}

type OrderController struct {
	newContributionOrderUsecase *order.NewContributionOrderUsecase
	additionalBuyOrderUsecase   *order.AdditionalBuyOrderUsecase
	rebalanceOrderUsecase       *order.RebalanceOrderUsecase
}

func NewOrderController(
	newContributionOrderUsecase *order.NewContributionOrderUsecase,
	additionalBuyOrderUsecase *order.AdditionalBuyOrderUsecase,
	rebalanceOrderUsecase *order.RebalanceOrderUsecase,
) *OrderController {
	return &OrderController{
		newContributionOrderUsecase: newContributionOrderUsecase,
		additionalBuyOrderUsecase:   additionalBuyOrderUsecase,
		rebalanceOrderUsecase:       rebalanceOrderUsecase,
	}
}

func (c *OrderController) NewContributionOrder(req NewContributionOrderRequest) error {
	uid, err := parseUserId(req.UserId)
	if err != nil {
		return err
	}
	amt, err := parseAmount(req.Amount)
	if err != nil {
		return err
	}

	err = c.newContributionOrderUsecase.Run(order.NewContributionOrderUsecaseInput{UserId: uid, Amount: amt})
	if err != nil {
		if errors.Is(err, order.ErrNewContributionUserAlreadyExists) {
			return newBadRequest("user already has account")
		}
		if errors.Is(err, order.ErrNewContributionAmountTooSmall) {
			return newBadRequest("amount is too small")
		}
		return err
	}
	return nil
}

func (c *OrderController) AdditionalContributionOrder(req AdditionalContributionOrderRequest) error {
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
