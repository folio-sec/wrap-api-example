<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Presentation;

use Folio\CodingInterview\Application\Usecase\Portfolio\GetLatestPortfolioUsecase;
use Folio\CodingInterview\Application\Usecase\Portfolio\UpdatePortfolioInvalidPortfolioException;
use Folio\CodingInterview\Application\Usecase\Portfolio\UpdatePortfolioItemInput;
use Folio\CodingInterview\Application\Usecase\Portfolio\UpdatePortfolioUsecase;
use Folio\CodingInterview\Application\Usecase\Portfolio\UpdatePortfolioUsecaseInput;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\StockSymbol;

final class PortfolioItemDto
{
    public function __construct(
        public readonly string $symbol,
        public readonly string $rate,
    ) {}
}

final class GetOptimalPortfolioResponse
{
    /** @param PortfolioItemDto[] $portfolios */
    public function __construct(public readonly array $portfolios) {}
}

final class UpdateOptimalPortfolioRequest
{
    /** @param PortfolioItemDto[] $portfolios */
    public function __construct(public readonly array $portfolios) {}
}

final class PortfolioController
{
    public function __construct(
        private readonly GetLatestPortfolioUsecase $getLatestPortfolioUsecase,
        private readonly UpdatePortfolioUsecase $updatePortfolioUsecase,
    ) {}

    public function getOptimalPortfolio(): GetOptimalPortfolioResponse
    {
        $out = $this->getLatestPortfolioUsecase->run();
        $items = [];
        foreach ($out->items as $i) {
            $items[] = new PortfolioItemDto($i->symbol->value, $i->rate->toString());
        }
        return new GetOptimalPortfolioResponse($items);
    }

    public function updateOptimalPortfolio(UpdateOptimalPortfolioRequest $req): void
    {
        $items = [];
        foreach ($req->portfolios as $dto) {
            $sym = StockSymbol::fromStringOrNull($dto->symbol);
            if ($sym === null) {
                throw new BadRequestException("unknown symbol: {$dto->symbol}");
            }
            try {
                $rate = new BigDecimal($dto->rate);
            } catch (\Throwable $e) {
                throw new BadRequestException("invalid rate: {$dto->rate}");
            }
            $items[] = new UpdatePortfolioItemInput($sym, $rate);
        }
        try {
            $this->updatePortfolioUsecase->run(new UpdatePortfolioUsecaseInput($items));
        } catch (UpdatePortfolioInvalidPortfolioException $e) {
            throw new BadRequestException($e->getMessage());
        }
    }
}
