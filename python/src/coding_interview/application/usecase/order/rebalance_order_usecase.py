from __future__ import annotations

from dataclasses import dataclass

from coding_interview.application.repository.account_repository import AccountRepository
from coding_interview.application.repository.market_price_repository import MarketPriceRepository
from coding_interview.application.repository.portfolio_repository import PortfolioRepository
from coding_interview.application.service.portfolio_service import rebalance
from coding_interview.application.usecase.exceptions import UserNotFoundError
from coding_interview.domain.user_id import UserId


@dataclass(frozen=True)
class RebalanceOrderUsecaseInput:
    user_id: UserId


class RebalanceOrderUsecase:
    def __init__(
        self,
        account_repository: AccountRepository,
        portfolio_repository: PortfolioRepository,
        market_price_repository: MarketPriceRepository,
    ) -> None:
        self._account_repository = account_repository
        self._portfolio_repository = portfolio_repository
        self._market_price_repository = market_price_repository

    def run(self, input: RebalanceOrderUsecaseInput) -> None:
        account = self._account_repository.find(input.user_id)
        if account is None:
            raise UserNotFoundError()
        portfolio = self._portfolio_repository.get()
        prices = self._market_price_repository.all()
        new_account = rebalance(account, portfolio, prices)
        self._account_repository.upsert(input.user_id, new_account)
