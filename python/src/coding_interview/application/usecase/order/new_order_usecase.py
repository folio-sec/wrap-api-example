from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.account_repository import AccountRepository
from coding_interview.application.repository.portfolio_repository import PortfolioRepository
from coding_interview.application.usecase.exceptions import AmountTooSmallError, UserAlreadyExistsError
from coding_interview.domain.account import Account
from coding_interview.domain.constants import MIN_OPERATION_AMOUNT
from coding_interview.domain.user_id import UserId


@dataclass(frozen=True)
class NewOrderUsecaseInput:
    user_id: UserId
    amount: Decimal


class NewOrderUsecase:
    def __init__(
        self,
        account_repository: AccountRepository,
        portfolio_repository: PortfolioRepository,
    ) -> None:
        self._account_repository = account_repository
        self._portfolio_repository = portfolio_repository

    def run(self, input: NewOrderUsecaseInput) -> None:
        if input.amount < MIN_OPERATION_AMOUNT:
            raise AmountTooSmallError()
        if self._account_repository.exists(input.user_id):
            raise UserAlreadyExistsError()
        portfolio = self._portfolio_repository.get()
        account = Account.open_account(input.amount, portfolio)
        self._account_repository.upsert(input.user_id, account)
