from __future__ import annotations

from decimal import Decimal

from coding_interview.domain.stock import Account, Stock
from coding_interview.domain.stock_symbol import StockSymbol


def evaluate_stock(stock: Stock, prices: dict[StockSymbol, Decimal]) -> Decimal:
    price = prices.get(stock.symbol)
    if price is None:
        raise IllegalStateError(f"missing price for {stock.symbol}")
    return stock.qty * price


def total_valuation(account: Account, prices: dict[StockSymbol, Decimal]) -> Decimal:
    return sum(evaluate_stock(s, prices) for s in account.stocks) + account.cash


class IllegalStateError(Exception):
    pass
