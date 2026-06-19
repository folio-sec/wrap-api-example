<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

/** 保有銘柄（銘柄と保有額）を表す。 */
final class Stock
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $amountJpy,
    ) {
    }
}
