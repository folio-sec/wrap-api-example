<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

final class Stock
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $qty,
    ) {
    }
}
