import uuid
from decimal import Decimal

import pytest

from coding_interview.infrastructure.server.dummy_server import DummyServer
from coding_interview.presentation.asset_controller import GetAssetRequest
from coding_interview.presentation.exceptions import BadRequestException
from coding_interview.presentation.market_price_controller import MarketPriceItemDto, UpdateMarketPriceRequest
from coding_interview.presentation.order_controller import (
    AdditionalContributionOrderRequest,
    NewContributionOrderRequest,
    RebalanceOrderRequest,
)
from coding_interview.presentation.portfolio_controller import PortfolioItemDto, UpdateOptimalPortfolioRequest


@pytest.fixture()
def server() -> DummyServer:
    s = DummyServer.default()
    # 各テスト前に価格とポートフォリオを初期化
    s.portfolio_controller.update_optimal_portfolio(
        UpdateOptimalPortfolioRequest(
            portfolios=(PortfolioItemDto("Toyopa", "0.40"), PortfolioItemDto("Somy", "0.60"))
        )
    )
    s.market_price_controller.update_market_price(
        UpdateMarketPriceRequest(
            market_prices=(MarketPriceItemDto("Toyopa", "2.5"), MarketPriceItemDto("Somy", "3.0"))
        )
    )
    return s


def test_investment_operation_scenario(server: DummyServer) -> None:
    ac = server.asset_controller
    pc = server.portfolio_controller
    oc = server.order_controller

    user_id = str(uuid.uuid4())

    # 存在しないユーザーで資産を取得しようとする
    with pytest.raises(BadRequestException):
        ac.get_asset(GetAssetRequest(user_id))
    # BadRequestException が返される

    # 最適ポートフォリオを Toyopa=40%, Somy=60% に更新する
    pc.update_optimal_portfolio(
        UpdateOptimalPortfolioRequest(
            portfolios=(PortfolioItemDto("Toyopa", "0.40"), PortfolioItemDto("Somy", "0.60"))
        )
    )

    # 新規拠出を 100,000 円で注文する
    oc.new_contribution_order(NewContributionOrderRequest(user_id, "100000"))

    asset1 = ac.get_asset(GetAssetRequest(user_id))
    assert {s.symbol for s in asset1.stocks} == {"Toyopa", "Somy"}
    total1 = Decimal(asset1.cashAmount) + sum(Decimal(s.evaluationAmount) for s in asset1.stocks)
    assert abs(total1 - Decimal("100000")) <= Decimal("2")

    # 現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の評価額が 38,000 円(40%)、Somy の評価額が 57,000 円(60%) となる
    asset1_toyopa = next(s for s in asset1.stocks if s.symbol == "Toyopa")
    asset1_somy = next(s for s in asset1.stocks if s.symbol == "Somy")
    assert Decimal(asset1_toyopa.evaluationAmount) == Decimal("38000")
    assert Decimal(asset1_somy.evaluationAmount) == Decimal("57000")
    assert Decimal(asset1.cashAmount) == Decimal("5000")

    # 追加拠出を 100,000 円で注文する
    oc.additional_contribution_order(AdditionalContributionOrderRequest(user_id, "100000"))

    # 資産合計が約 200,000 円になる
    asset2 = ac.get_asset(GetAssetRequest(user_id))
    total2 = Decimal(asset2.cashAmount) + sum(Decimal(s.evaluationAmount) for s in asset2.stocks)
    assert abs(total2 - Decimal("200000")) <= Decimal("4")

    # 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 76,000 円(40%)、Somy の評価額が 114,000 円(60%) となる
    asset2_toyopa = next(s for s in asset2.stocks if s.symbol == "Toyopa")
    asset2_somy = next(s for s in asset2.stocks if s.symbol == "Somy")
    assert Decimal(asset2_toyopa.evaluationAmount) == Decimal("76000")
    assert Decimal(asset2_somy.evaluationAmount) == Decimal("114000")
    assert Decimal(asset2.cashAmount) == Decimal("10000")

    # 最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする
    pc.update_optimal_portfolio(
        UpdateOptimalPortfolioRequest(
            portfolios=(PortfolioItemDto("Toyopa", "0.10"), PortfolioItemDto("Somy", "0.90"))
        )
    )
    oc.rebalance_order(RebalanceOrderRequest(user_id))

    # リバランス後も資産合計がほぼ変わらない
    asset3 = ac.get_asset(GetAssetRequest(user_id))
    total3 = Decimal(asset3.cashAmount) + sum(Decimal(s.evaluationAmount) for s in asset3.stocks)
    assert abs(total3 - total2) <= Decimal("4")

    # 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 19,000 円(10%)、Somy の評価額が 171,000 円(90%) となる
    asset3_toyopa = next(s for s in asset3.stocks if s.symbol == "Toyopa")
    asset3_somy = next(s for s in asset3.stocks if s.symbol == "Somy")
    assert Decimal(asset3_toyopa.evaluationAmount) == Decimal("19000")
    assert Decimal(asset3_somy.evaluationAmount) == Decimal("171000")
    assert Decimal(asset3.cashAmount) == Decimal("10000")
