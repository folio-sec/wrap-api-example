from __future__ import annotations

from decimal import Decimal

from coding_interview.domain.stock import Portfolio, PortfolioItem
from coding_interview.domain.stock_symbol import StockSymbol

CASH_RATE: Decimal = Decimal("0.05")
MIN_OPERATION_AMOUNT: Decimal = Decimal("10000")

SUPPORTED_SYMBOLS: tuple[StockSymbol, ...] = (StockSymbol.Toyopa, StockSymbol.Somy)

INITIAL_PRICES: dict[StockSymbol, Decimal] = {
    StockSymbol.Toyopa: Decimal("4.2135"),
    StockSymbol.Somy: Decimal("1.2345"),
}

INITIAL_PORTFOLIO: Portfolio = Portfolio(
    items=(
        PortfolioItem(StockSymbol.Toyopa, Decimal("0.40")),
        PortfolioItem(StockSymbol.Somy, Decimal("0.60")),
    )
)
