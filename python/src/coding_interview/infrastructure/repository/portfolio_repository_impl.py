from __future__ import annotations

from coding_interview.domain.constants import INITIAL_PORTFOLIO
from coding_interview.domain.stock import Portfolio


class PortfolioRepositoryImpl:
    def __init__(self) -> None:
        self._portfolio: Portfolio = INITIAL_PORTFOLIO

    def get(self) -> Portfolio:
        return self._portfolio

    def update(self, portfolio: Portfolio) -> None:
        self._portfolio = portfolio
