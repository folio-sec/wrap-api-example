<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

final class Account
{
    /** @param Stock[] $stocks */
    public function __construct(
        public readonly BigDecimal $cash,
        public readonly array $stocks,
    ) {
    }
}
