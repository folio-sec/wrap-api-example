<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Presentation;

use Folio\CodingInterview\Application\Usecase\MarketPrice\UpdateMarketPriceItemInput;
use Folio\CodingInterview\Application\Usecase\MarketPrice\UpdateMarketPriceUsecase;
use Folio\CodingInterview\Application\Usecase\MarketPrice\UpdateMarketPriceUsecaseInput;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\StockSymbol;

final class MarketPriceItemDto
{
    public function __construct(
        public readonly string $symbol,
        public readonly string $market_price,
    ) {}
}

final class UpdateMarketPriceRequest
{
    /** @param MarketPriceItemDto[] $market_prices */
    public function __construct(public readonly array $market_prices) {}
}

final class MarketPriceController
{
    public function __construct(private readonly UpdateMarketPriceUsecase $updateMarketPriceUsecase) {}

    public function updateMarketPrice(UpdateMarketPriceRequest $req): void
    {
        $items = [];
        foreach ($req->market_prices as $dto) {
            $sym = StockSymbol::fromStringOrNull($dto->symbol);
            if ($sym === null) {
                throw new BadRequestException("unknown symbol: {$dto->symbol}");
            }
            try {
                $price = new BigDecimal($dto->market_price);
            } catch (\Throwable $e) {
                throw new BadRequestException("invalid market_price: {$dto->market_price}");
            }
            $items[] = new UpdateMarketPriceItemInput($sym, $price);
        }
        $this->updateMarketPriceUsecase->run(new UpdateMarketPriceUsecaseInput($items));
    }
}
