<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

/** ポートフォリオの銘柄ごとの構成比率を表す。 */
final class PortfolioItem
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $rate,
    ) {
    }
}
