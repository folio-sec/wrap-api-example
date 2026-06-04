from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal, InvalidOperation

from coding_interview.application.usecase.market_price.update_market_price_usecase import (
    UpdateMarketPriceItemInput,
    UpdateMarketPriceUsecase,
    UpdateMarketPriceUsecaseInput,
)
from coding_interview.domain.stock_symbol import StockSymbol
from coding_interview.presentation.exceptions import BadRequestException


@dataclass(frozen=True)
class MarketPriceItemDto:
    symbol: str
    market_price: str


@dataclass(frozen=True)
class UpdateMarketPriceRequest:
    market_prices: tuple[MarketPriceItemDto, ...]


class MarketPriceController:
    def __init__(self, update_market_price_usecase: UpdateMarketPriceUsecase) -> None:
        self._update_market_price_usecase = update_market_price_usecase

    def update_market_price(self, req: UpdateMarketPriceRequest) -> None:
        items: list[UpdateMarketPriceItemInput] = []
        for dto in req.market_prices:
            sym = StockSymbol.from_string(dto.symbol)
            if sym is None:
                raise BadRequestException(f"unknown symbol: {dto.symbol}")
            try:
                price = Decimal(dto.market_price)
            except (InvalidOperation, Exception):
                raise BadRequestException(f"invalid market_price: {dto.market_price}")
            items.append(UpdateMarketPriceItemInput(sym, price))
        self._update_market_price_usecase.run(UpdateMarketPriceUsecaseInput(tuple(items)))
