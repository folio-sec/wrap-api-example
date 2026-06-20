from __future__ import annotations

from dataclasses import dataclass

from coding_interview.application.usecase.asset.get_asset_usecase import (
    GetAssetUsecase,
    GetAssetUsecaseInput,
)
from coding_interview.application.usecase.exceptions import UserNotFoundError
from coding_interview.presentation.exceptions import BadRequestException
from coding_interview.presentation.preparation import parse_user_id


@dataclass(frozen=True)
class StockDto:
    symbol: str
    amountJpy: str


@dataclass(frozen=True)
class GetAssetRequest:
    userId: str


@dataclass(frozen=True)
class GetAssetResponse:
    cashAmount: str
    stocks: tuple[StockDto, ...]


class AssetController:
    def __init__(self, get_asset_usecase: GetAssetUsecase) -> None:
        self._get_asset_usecase = get_asset_usecase

    def get_asset(self, req: GetAssetRequest) -> GetAssetResponse:
        uid = parse_user_id(req.userId)
        try:
            out = self._get_asset_usecase.run(GetAssetUsecaseInput(uid))
        except UserNotFoundError:
            raise BadRequestException("user not found")
        return GetAssetResponse(
            cashAmount=str(out.cash_amount),
            stocks=tuple(StockDto(symbol=str(s.symbol), amountJpy=str(s.amount_jpy)) for s in out.stocks),
        )
