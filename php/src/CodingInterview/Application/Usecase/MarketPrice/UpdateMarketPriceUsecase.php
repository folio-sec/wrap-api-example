<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\MarketPrice;

use Folio\CodingInterview\Application\Repository\MarketPriceRepository;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\StockSymbol;

final class UpdateMarketPriceItemInput
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $marketPrice,
    ) {}
}

final class UpdateMarketPriceUsecaseInput
{
    /** @param UpdateMarketPriceItemInput[] $items */
    public function __construct(public readonly array $items) {}
}

final class UpdateMarketPriceUsecase
{
    public function __construct(private readonly MarketPriceRepository $marketPriceRepository) {}

    public function run(UpdateMarketPriceUsecaseInput $input): void
    {
        $prices = [];
        foreach ($input->items as $i) {
            $prices[$i->symbol->value] = $i->marketPrice;
        }
        $this->marketPriceRepository->update($prices);
    }
}
