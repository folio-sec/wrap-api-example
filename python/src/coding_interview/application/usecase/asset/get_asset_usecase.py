from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.account_repository import AccountRepository
from coding_interview.application.usecase.exceptions import UserNotFoundError
from coding_interview.domain.stock_symbol import StockSymbol
from coding_interview.domain.user_id import UserId


@dataclass(frozen=True)
class GetAssetUsecaseInput:
    user_id: UserId


@dataclass(frozen=True)
class GetAssetStockOutput:
    symbol: StockSymbol
    amount_jpy: Decimal


@dataclass(frozen=True)
class GetAssetUsecaseOutput:
    cash_amount: Decimal
    stocks: tuple[GetAssetStockOutput, ...]


class GetAssetUsecase:
    def __init__(self, account_repository: AccountRepository) -> None:
        self._account_repository = account_repository

    def run(self, input: GetAssetUsecaseInput) -> GetAssetUsecaseOutput:
        account = self._account_repository.find(input.user_id)
        if account is None:
            raise UserNotFoundError()
        stocks = tuple(
            GetAssetStockOutput(symbol=s.symbol, amount_jpy=s.amount_jpy)
            for s in account.stocks
        )
        return GetAssetUsecaseOutput(cash_amount=account.cash, stocks=stocks)
