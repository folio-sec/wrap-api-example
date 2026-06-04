from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.account_repository import AccountRepository
from coding_interview.application.repository.market_price_repository import MarketPriceRepository
from coding_interview.application.repository.portfolio_repository import PortfolioRepository
from coding_interview.application.service.portfolio_service import allocate_new
from coding_interview.application.usecase.exceptions import AmountTooSmallError, UserAlreadyExistsError
from coding_interview.domain.constants import MIN_OPERATION_AMOUNT
from coding_interview.domain.user_id import UserId


@dataclass(frozen=True)
class NewContributionOrderUsecaseInput:
    user_id: UserId
    amount: Decimal


class NewContributionOrderUsecase:
    def __init__(
        self,
        account_repository: AccountRepository,
        portfolio_repository: PortfolioRepository,
        market_price_repository: MarketPriceRepository,
    ) -> None:
        self._account_repository = account_repository
        self._portfolio_repository = portfolio_repository
        self._market_price_repository = market_price_repository

    def run(self, input: NewContributionOrderUsecaseInput) -> None:
        if input.amount < MIN_OPERATION_AMOUNT:
            raise AmountTooSmallError()
        if self._account_repository.exists(input.user_id):
            raise UserAlreadyExistsError()
        portfolio = self._portfolio_repository.get()
        prices = self._market_price_repository.all()
        account = allocate_new(input.amount, portfolio, prices)
        self._account_repository.upsert(input.user_id, account)
