from decimal import Decimal

import pytest

from coding_interview.infrastructure.server.dummy_server import DummyServer
from coding_interview.presentation.portfolio_controller import PortfolioItemDto, UpdateOptimalPortfolioRequest


@pytest.fixture()
def server() -> DummyServer:
    return DummyServer.default()


def test_optimal_portfolio_scenario(server: DummyServer) -> None:
    pc = server.portfolio_controller

    # Toyopa=0.20 / Somy=0.80 に更新して確認
    pc.update_optimal_portfolio(
        UpdateOptimalPortfolioRequest(
            portfolios=(PortfolioItemDto("Toyopa", "0.20"), PortfolioItemDto("Somy", "0.80"))
        )
    )
    resp1 = pc.get_optimal_portfolio()
    rates1 = {item.symbol: Decimal(item.rate) for item in resp1.portfolios}
    assert rates1["Toyopa"] == Decimal("0.20")
    assert rates1["Somy"] == Decimal("0.80")

    # Toyopa=0.40 / Somy=0.60 に更新して確認
    pc.update_optimal_portfolio(
        UpdateOptimalPortfolioRequest(
            portfolios=(PortfolioItemDto("Toyopa", "0.40"), PortfolioItemDto("Somy", "0.60"))
        )
    )
    resp2 = pc.get_optimal_portfolio()
    rates2 = {item.symbol: Decimal(item.rate) for item in resp2.portfolios}
    assert rates2["Toyopa"] == Decimal("0.40")
    assert rates2["Somy"] == Decimal("0.60")
