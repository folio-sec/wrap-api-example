from __future__ import annotations

from dataclasses import dataclass
from decimal import Decimal, ROUND_DOWN

from coding_interview.domain.stock import Portfolio, Stock
from coding_interview.domain.stock_symbol import StockSymbol

_ZERO_DP = Decimal("1")


def _floor0(x: Decimal) -> Decimal:
    """円未満を切り捨てる（資産配分はすべて円単位で行う）。"""
    return x.quantize(_ZERO_DP, rounding=ROUND_DOWN)


@dataclass(frozen=True)
class Account:
    """口座を表す。"""

    cash: Decimal
    stocks: tuple[Stock, ...]

    def total(self) -> Decimal:
        """口座の総資産（現金 + 各銘柄の保有額）を返す。"""
        return self.cash + sum(s.amount_jpy for s in self.stocks)

    @classmethod
    def open_account(cls, amount: Decimal, portfolio: Portfolio) -> "Account":
        """新規注文額を、最適ポートフォリオに沿って配分した口座を生成する。"""
        from coding_interview.domain.constants import CASH_RATE

        cash_from_rate = _floor0(amount * CASH_RATE)
        investable = amount - cash_from_rate
        stocks: list[Stock] = []
        used_for_stocks = Decimal(0)
        for item in portfolio.items:
            amt = _floor0(investable * item.rate)
            stocks.append(Stock(item.symbol, amt))
            used_for_stocks += amt
        residual = investable - used_for_stocks
        return cls(cash=cash_from_rate + residual, stocks=tuple(stocks))

    def add_funds(self, amount: Decimal, portfolio: Portfolio) -> "Account":
        """追加注文額を口座へ反映する。最適ポートフォリオの目標額を下回らない範囲で
        既存の保有額を維持し、ポートフォリオ外の銘柄はそのまま保持する。"""
        from coding_interview.domain.constants import CASH_RATE

        total_after = self.total() + amount
        target_cash = _floor0(total_after * CASH_RATE)
        investable = total_after - target_cash

        current_amount: dict[StockSymbol, Decimal] = {s.symbol: s.amount_jpy for s in self.stocks}
        portfolio_symbols = {item.symbol for item in portfolio.items}

        new_portfolio_stocks: list[Stock] = []
        for item in portfolio.items:
            target = _floor0(investable * item.rate)
            current = current_amount.get(item.symbol, Decimal(0))
            final = current if current > target else target
            new_portfolio_stocks.append(Stock(item.symbol, final))

        preserved_stocks = [s for s in self.stocks if s.symbol not in portfolio_symbols]
        all_stocks = tuple(new_portfolio_stocks + preserved_stocks)
        final_amount = sum(s.amount_jpy for s in all_stocks)
        return Account(cash=total_after - final_amount, stocks=all_stocks)

    def rebalance(self, portfolio: Portfolio) -> "Account":
        """保有資産を最適ポートフォリオの比率に近づける。"""
        # XXX this implementation might not be correct
        investable = self.total()
        new_stocks: list[Stock] = []
        used_for_stocks = Decimal(0)
        for item in portfolio.items:
            amt = _floor0(investable * item.rate)
            new_stocks.append(Stock(item.symbol, amt))
            used_for_stocks += amt
        final_cash = investable - used_for_stocks
        return Account(cash=final_cash, stocks=tuple(new_stocks))
