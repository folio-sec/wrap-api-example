from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.account_repository import AccountRepository
from coding_interview.application.repository.market_price_repository import MarketPriceRepository
from coding_interview.application.service.asset_service import evaluate_stock
from coding_interview.application.usecase.exceptions import UserNotFoundError
from coding_interview.domain.stock_symbol import StockSymbol
from coding_interview.domain.user_id import UserId


@dataclass(frozen=True)
class GetAssetUsecaseInput:
    user_id: UserId


@dataclass(frozen=True)
class GetAssetStockOutput:
    symbol: StockSymbol
    evaluation_amount: Decimal


@dataclass(frozen=True)
class GetAssetUsecaseOutput:
    cash_amount: Decimal
    stocks: tuple[GetAssetStockOutput, ...]


class GetAssetUsecase:
    def __init__(
        self,
        account_repository: AccountRepository,
        market_price_repository: MarketPriceRepository,
    ) -> None:
        self._account_repository = account_repository
        self._market_price_repository = market_price_repository

    def run(self, input: GetAssetUsecaseInput) -> GetAssetUsecaseOutput:
        account = self._account_repository.find(input.user_id)
        if account is None:
            raise UserNotFoundError()
        prices = self._market_price_repository.all()
        stocks = tuple(
            GetAssetStockOutput(symbol=s.symbol, evaluation_amount=evaluate_stock(s, prices))
            for s in account.stocks
        )
        return GetAssetUsecaseOutput(cash_amount=account.cash, stocks=stocks)
