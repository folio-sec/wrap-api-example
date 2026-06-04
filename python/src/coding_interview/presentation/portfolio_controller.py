from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal, InvalidOperation

from coding_interview.application.usecase.exceptions import InvalidPortfolioError
from coding_interview.application.usecase.portfolio.get_latest_portfolio_usecase import GetLatestPortfolioUsecase
from coding_interview.application.usecase.portfolio.update_portfolio_usecase import (
    UpdatePortfolioItemInput,
    UpdatePortfolioUsecase,
    UpdatePortfolioUsecaseInput,
)
from coding_interview.domain.stock_symbol import StockSymbol
from coding_interview.presentation.exceptions import BadRequestException


@dataclass(frozen=True)
class PortfolioItemDto:
    symbol: str
    rate: str


@dataclass(frozen=True)
class GetOptimalPortfolioResponse:
    portfolios: tuple[PortfolioItemDto, ...]


@dataclass(frozen=True)
class UpdateOptimalPortfolioRequest:
    portfolios: tuple[PortfolioItemDto, ...]


class PortfolioController:
    def __init__(
        self,
        get_latest_portfolio_usecase: GetLatestPortfolioUsecase,
        update_portfolio_usecase: UpdatePortfolioUsecase,
    ) -> None:
        self._get_latest_portfolio_usecase = get_latest_portfolio_usecase
        self._update_portfolio_usecase = update_portfolio_usecase

    def get_optimal_portfolio(self) -> GetOptimalPortfolioResponse:
        out = self._get_latest_portfolio_usecase.run()
        return GetOptimalPortfolioResponse(
            portfolios=tuple(PortfolioItemDto(symbol=str(i.symbol), rate=str(i.rate)) for i in out.items)
        )

    def update_optimal_portfolio(self, req: UpdateOptimalPortfolioRequest) -> None:
        items: list[UpdatePortfolioItemInput] = []
        for dto in req.portfolios:
            sym = StockSymbol.from_string(dto.symbol)
            if sym is None:
                raise BadRequestException(f"unknown symbol: {dto.symbol}")
            try:
                rate = Decimal(dto.rate)
            except (InvalidOperation, Exception):
                raise BadRequestException(f"invalid rate: {dto.rate}")
            items.append(UpdatePortfolioItemInput(sym, rate))
        try:
            self._update_portfolio_usecase.run(UpdatePortfolioUsecaseInput(tuple(items)))
        except InvalidPortfolioError as e:
            raise BadRequestException(e.reason)
