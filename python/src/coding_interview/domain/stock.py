from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.domain.stock_symbol import StockSymbol


# Stock は保有銘柄（銘柄と保有額）を表す。
@dataclass(frozen=True)
class Stock:
    symbol: StockSymbol
    amount_jpy: Decimal


# PortfolioItem はポートフォリオの銘柄ごとの構成比率を表す。
@dataclass(frozen=True)
class PortfolioItem:
    symbol: StockSymbol
    rate: Decimal


# Portfolio は最適ポートフォリオ（銘柄ごとの構成比率）を表す。
@dataclass(frozen=True)
class Portfolio:
    items: tuple[PortfolioItem, ...]

    def __post_init__(self) -> None:
        if not self.items:
            raise ValueError("portfolio must have at least one item")
        rate_sum = sum(item.rate for item in self.items)
        if rate_sum != Decimal(1):
            raise ValueError(f"portfolio rates must sum to 1, got {rate_sum}")
        symbols = [item.symbol for item in self.items]
        if len(symbols) != len(set(symbols)):
            raise ValueError("portfolio must not have duplicate symbols")
