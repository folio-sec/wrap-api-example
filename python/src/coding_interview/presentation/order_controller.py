from __future__ import annotations

from dataclasses import dataclass

from coding_interview.application.usecase.exceptions import (
    AmountTooSmallError,
    UserAlreadyExistsError,
    UserNotFoundError,
)
from coding_interview.application.usecase.order.additional_buy_order_usecase import (
    AdditionalBuyOrderUsecase,
    AdditionalBuyOrderUsecaseInput,
)
from coding_interview.application.usecase.order.new_order_usecase import (
    NewOrderUsecase,
    NewOrderUsecaseInput,
)
from coding_interview.application.usecase.order.rebalance_order_usecase import (
    RebalanceOrderUsecase,
    RebalanceOrderUsecaseInput,
)
from coding_interview.presentation.exceptions import BadRequestException
from coding_interview.presentation.preparation import parse_amount, parse_user_id


@dataclass(frozen=True)
class NewOrderRequest:
    userId: str
    amount: str


@dataclass(frozen=True)
class AdditionalOrderRequest:
    userId: str
    amount: str


@dataclass(frozen=True)
class RebalanceOrderRequest:
    userId: str


class OrderController:
    def __init__(
        self,
        new_order_usecase: NewOrderUsecase,
        additional_buy_order_usecase: AdditionalBuyOrderUsecase,
        rebalance_order_usecase: RebalanceOrderUsecase,
    ) -> None:
        self._new_order_usecase = new_order_usecase
        self._additional_buy_order_usecase = additional_buy_order_usecase
        self._rebalance_order_usecase = rebalance_order_usecase

    def new_order(self, req: NewOrderRequest) -> None:
        uid = parse_user_id(req.userId)
        amt = parse_amount(req.amount)
        try:
            self._new_order_usecase.run(NewOrderUsecaseInput(uid, amt))
        except UserAlreadyExistsError:
            raise BadRequestException("user already has account")
        except AmountTooSmallError:
            raise BadRequestException("amount is too small")

    def additional_order(self, req: AdditionalOrderRequest) -> None:
        uid = parse_user_id(req.userId)
        amt = parse_amount(req.amount)
        try:
            self._additional_buy_order_usecase.run(AdditionalBuyOrderUsecaseInput(uid, amt))
        except UserNotFoundError:
            raise BadRequestException("user has no live account")
        except AmountTooSmallError:
            raise BadRequestException("amount is too small")

    def rebalance_order(self, req: RebalanceOrderRequest) -> None:
        uid = parse_user_id(req.userId)
        try:
            self._rebalance_order_usecase.run(RebalanceOrderUsecaseInput(uid))
        except UserNotFoundError:
            raise BadRequestException("user has no live account")
