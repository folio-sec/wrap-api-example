from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.account_repository import AccountRepository
from coding_interview.application.repository.portfolio_repository import PortfolioRepository
from coding_interview.application.usecase.exceptions import AmountTooSmallError, UserNotFoundError
from coding_interview.domain.constants import MIN_OPERATION_AMOUNT
from coding_interview.domain.user_id import UserId


@dataclass(frozen=True)
class AdditionalBuyOrderUsecaseInput:
    user_id: UserId
    amount: Decimal


class AdditionalBuyOrderUsecase:
    def __init__(
        self,
        account_repository: AccountRepository,
        portfolio_repository: PortfolioRepository,
    ) -> None:
        self._account_repository = account_repository
        self._portfolio_repository = portfolio_repository

    def run(self, input: AdditionalBuyOrderUsecaseInput) -> None:
        if input.amount < MIN_OPERATION_AMOUNT:
            raise AmountTooSmallError()
        account = self._account_repository.find(input.user_id)
        if account is None:
            raise UserNotFoundError()
        portfolio = self._portfolio_repository.get()
        new_account = account.add_funds(input.amount, portfolio)
        self._account_repository.upsert(input.user_id, new_account)
