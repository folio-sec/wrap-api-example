from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.portfolio_repository import PortfolioRepository
from coding_interview.application.usecase.exceptions import InvalidPortfolioError
from coding_interview.domain.stock import Portfolio, PortfolioItem
from coding_interview.domain.stock_symbol import StockSymbol


@dataclass(frozen=True)
class UpdatePortfolioItemInput:
    symbol: StockSymbol
    rate: Decimal


@dataclass(frozen=True)
class UpdatePortfolioUsecaseInput:
    items: tuple[UpdatePortfolioItemInput, ...]


class UpdatePortfolioUsecase:
    def __init__(self, portfolio_repository: PortfolioRepository) -> None:
        self._portfolio_repository = portfolio_repository

    def run(self, input: UpdatePortfolioUsecaseInput) -> None:
        try:
            portfolio = Portfolio(
                items=tuple(PortfolioItem(i.symbol, i.rate) for i in input.items)
            )
        except (ValueError, Exception) as e:
            raise InvalidPortfolioError(str(e)) from e
        self._portfolio_repository.update(portfolio)
