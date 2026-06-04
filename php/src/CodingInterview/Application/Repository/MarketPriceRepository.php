<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Repository;

use Folio\CodingInterview\Domain\BigDecimal;

/**
 * 市場価格リポジトリ。
 */
interface MarketPriceRepository
{
    /** @return array<string,BigDecimal> keyed by StockSymbol->value */
    public function all(): array;

    /** @param array<string,BigDecimal> $prices */
    public function update(array $prices): void;
}
