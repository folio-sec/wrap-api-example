<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

/** 最適ポートフォリオ（銘柄ごとの構成比率）を表す。 */
final class Portfolio
{
    /** @param PortfolioItem[] $items */
    public function __construct(public readonly array $items)
    {
        if (count($items) === 0) {
            throw new \InvalidArgumentException('portfolio must have at least one item');
        }
        $sum = BigDecimal::zero();
        $symbols = [];
        foreach ($items as $i) {
            $sum = $sum->add($i->rate);
            $symbols[] = $i->symbol->value;
        }
        if (!$sum->eq(BigDecimal::of(1))) {
            throw new \InvalidArgumentException("portfolio rates must sum to 1, got {$sum}");
        }
        if (count(array_unique($symbols)) !== count($symbols)) {
            throw new \InvalidArgumentException('portfolio must not have duplicate symbols');
        }
    }
}
