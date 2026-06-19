from __future__ import annotations

from enum import Enum


class StockSymbol(Enum):
    """銘柄を表す。"""

    Toyopa = "Toyopa"
    Somy = "Somy"

    @classmethod
    def from_string(cls, s: str) -> "StockSymbol | None":
        try:
            return cls(s)
        except ValueError:
            return None

    def __str__(self) -> str:
        return self.value
