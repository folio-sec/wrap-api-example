from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.portfolio_repository import PortfolioRepository
from coding_interview.domain.stock_symbol import StockSymbol


@dataclass(frozen=True)
class GetLatestPortfolioItemOutput:
    symbol: StockSymbol
    rate: Decimal


@dataclass(frozen=True)
class GetLatestPortfolioUsecaseOutput:
    items: tuple[GetLatestPortfolioItemOutput, ...]


class GetLatestPortfolioUsecase:
    def __init__(self, portfolio_repository: PortfolioRepository) -> None:
        self._portfolio_repository = portfolio_repository

    def run(self) -> GetLatestPortfolioUsecaseOutput:
        portfolio = self._portfolio_repository.get()
        items = tuple(
            GetLatestPortfolioItemOutput(symbol=item.symbol, rate=item.rate)
            for item in portfolio.items
        )
        return GetLatestPortfolioUsecaseOutput(items=items)
