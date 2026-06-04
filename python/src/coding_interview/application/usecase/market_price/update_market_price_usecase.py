from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal

from coding_interview.application.repository.market_price_repository import MarketPriceRepository
from coding_interview.domain.stock_symbol import StockSymbol


@dataclass(frozen=True)
class UpdateMarketPriceItemInput:
    symbol: StockSymbol
    market_price: Decimal


@dataclass(frozen=True)
class UpdateMarketPriceUsecaseInput:
    items: tuple[UpdateMarketPriceItemInput, ...]


class UpdateMarketPriceUsecase:
    def __init__(self, market_price_repository: MarketPriceRepository) -> None:
        self._market_price_repository = market_price_repository

    def run(self, input: UpdateMarketPriceUsecaseInput) -> None:
        prices = {item.symbol: item.market_price for item in input.items}
        self._market_price_repository.update(prices)
