<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Portfolio;

use Folio\CodingInterview\Application\Repository\PortfolioRepository;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\StockSymbol;

final class GetLatestPortfolioItemOutput
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $rate,
    ) {}
}

final class GetLatestPortfolioUsecaseOutput
{
    /** @param GetLatestPortfolioItemOutput[] $items */
    public function __construct(public readonly array $items) {}
}

final class GetLatestPortfolioUsecase
{
    public function __construct(private readonly PortfolioRepository $portfolioRepository) {}

    public function run(): GetLatestPortfolioUsecaseOutput
    {
        $p = $this->portfolioRepository->get();
        $items = [];
        foreach ($p->items as $i) {
            $items[] = new GetLatestPortfolioItemOutput($i->symbol, $i->rate);
        }
        return new GetLatestPortfolioUsecaseOutput($items);
    }
}
