from __future__ import annotations

from decimal import Decimal

from coding_interview.domain.constants import INITIAL_PRICES
from coding_interview.domain.stock_symbol import StockSymbol


class MarketPriceRepositoryImpl:
    def __init__(self) -> None:
        self._prices: dict[StockSymbol, Decimal] = dict(INITIAL_PRICES)

    def all(self) -> dict[StockSymbol, Decimal]:
        return dict(self._prices)

    def update(self, prices: dict[StockSymbol, Decimal]) -> None:
        self._prices = dict(prices)
