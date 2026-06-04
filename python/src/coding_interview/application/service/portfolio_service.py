from __future__ import annotations

from decimal import Decimal, ROUND_DOWN

from coding_interview.domain.constants import CASH_RATE
from coding_interview.domain.stock import Account, Stock, Portfolio
from coding_interview.domain.stock_symbol import StockSymbol
from coding_interview.application.service.asset_service import total_valuation

_TWO_DP = Decimal("0.01")
_ZERO_DP = Decimal("1")


def _floor2(x: Decimal) -> Decimal:
    return x.quantize(_TWO_DP, rounding=ROUND_DOWN)


def _floor0(x: Decimal) -> Decimal:
    return x.quantize(_ZERO_DP, rounding=ROUND_DOWN)


def _price_of(prices: dict[StockSymbol, Decimal], symbol: StockSymbol) -> Decimal:
    price = prices.get(symbol)
    if price is None:
        raise ValueError(f"missing price for {symbol}")
    return price


def allocate_new(
    amount: Decimal,
    portfolio: Portfolio,
    prices: dict[StockSymbol, Decimal],
) -> Account:
    cash_from_rate = _floor0(amount * CASH_RATE)
    investable = amount - cash_from_rate
    stocks = tuple(
        Stock(item.symbol, _floor2(investable * item.rate / _price_of(prices, item.symbol)))
        for item in portfolio.items
    )
    used_for_stocks = sum(s.qty * _price_of(prices, s.symbol) for s in stocks)
    residual = investable - used_for_stocks
    return Account(cash=cash_from_rate + residual, stocks=stocks)


def allocate_additional(
    account: Account,
    amount: Decimal,
    portfolio: Portfolio,
    prices: dict[StockSymbol, Decimal],
) -> Account:
    total_after = total_valuation(account, prices) + amount
    target_cash = _floor0(total_after * CASH_RATE)
    investable = total_after - target_cash
    current_qty: dict[StockSymbol, Decimal] = {s.symbol: s.qty for s in account.stocks}
    portfolio_symbols = {item.symbol for item in portfolio.items}

    new_portfolio_stocks = []
    for item in portfolio.items:
        target_qty = _floor2(investable * item.rate / _price_of(prices, item.symbol))
        current = current_qty.get(item.symbol, Decimal(0))
        final_qty = target_qty if target_qty > current else current
        new_portfolio_stocks.append(Stock(item.symbol, final_qty))

    preserved_stocks = [s for s in account.stocks if s.symbol not in portfolio_symbols]
    all_stocks = tuple(new_portfolio_stocks + preserved_stocks)

    final_valuation = sum(s.qty * _price_of(prices, s.symbol) for s in all_stocks)
    return Account(cash=total_after - final_valuation, stocks=all_stocks)


def rebalance(
    account: Account,
    portfolio: Portfolio,
    prices: dict[StockSymbol, Decimal],
) -> Account:
    # XXX this implementation might not be correct
    investable = total_valuation(account, prices)
    new_stocks = tuple(
        Stock(item.symbol, _floor2(investable * item.rate / _price_of(prices, item.symbol)))
        for item in portfolio.items
    )
    final_valuation = sum(s.qty * _price_of(prices, s.symbol) for s in new_stocks)
    return Account(cash=investable - final_valuation, stocks=new_stocks)
