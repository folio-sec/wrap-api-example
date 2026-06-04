from __future__ import annotations

from typing import Protocol

from coding_interview.domain.stock import Portfolio


class PortfolioRepository(Protocol):
    """最適ポートフォリオリポジトリ。"""

    def get(self) -> Portfolio: ...
    def update(self, portfolio: Portfolio) -> None: ...
