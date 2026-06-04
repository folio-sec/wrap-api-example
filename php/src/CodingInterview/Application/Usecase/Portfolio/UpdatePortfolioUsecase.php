<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Portfolio;

use Folio\CodingInterview\Application\Repository\PortfolioRepository;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\StockSymbol;
use Folio\CodingInterview\Domain\Portfolio;
use Folio\CodingInterview\Domain\PortfolioItem;

final class UpdatePortfolioItemInput
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $rate,
    ) {}
}

final class UpdatePortfolioUsecaseInput
{
    /** @param UpdatePortfolioItemInput[] $items */
    public function __construct(public readonly array $items) {}
}

final class UpdatePortfolioInvalidPortfolioException extends \RuntimeException
{
}

final class UpdatePortfolioUsecase
{
    public function __construct(private readonly PortfolioRepository $portfolioRepository) {}

    public function run(UpdatePortfolioUsecaseInput $input): void
    {
        try {
            $items = [];
            foreach ($input->items as $i) {
                $items[] = new PortfolioItem($i->symbol, $i->rate);
            }
            $portfolio = new Portfolio($items);
        } catch (\Throwable $e) {
            throw new UpdatePortfolioInvalidPortfolioException($e->getMessage(), 0, $e);
        }
        $this->portfolioRepository->update($portfolio);
    }
}
