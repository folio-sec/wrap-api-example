<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Infrastructure\Repository;

use Folio\CodingInterview\Application\Repository\MarketPriceRepository;
use Folio\CodingInterview\Domain\AppConstants;
use Folio\CodingInterview\Domain\BigDecimal;

final class MarketPriceRepositoryImpl implements MarketPriceRepository
{
    /** @var array<string,BigDecimal> */
    private array $prices;

    public function __construct()
    {
        $this->prices = AppConstants::initialPrices();
    }

    public function all(): array
    {
        return $this->prices;
    }

    public function update(array $prices): void
    {
        $this->prices = $prices;
    }
}
