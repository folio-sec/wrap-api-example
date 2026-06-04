from __future__ import annotations

from decimal import Decimal
from typing import Protocol

from coding_interview.domain.stock_symbol import StockSymbol


class MarketPriceRepository(Protocol):
    """市場価格リポジトリ。"""

    def all(self) -> dict[StockSymbol, Decimal]: ...
    def update(self, prices: dict[StockSymbol, Decimal]) -> None: ...
