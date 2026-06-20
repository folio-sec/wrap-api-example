<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

final class AppConstants
{
    public static function cashRate(): BigDecimal
    {
        return new BigDecimal('0.05');
    }

    public static function minOperationAmount(): BigDecimal
    {
        return new BigDecimal('10000');
    }

    /** @return StockSymbol[] */
    public static function supportedSymbols(): array
    {
        return [StockSymbol::Toyopa, StockSymbol::Somy];
    }

    public static function initialPortfolio(): Portfolio
    {
        return new Portfolio([
            new PortfolioItem(StockSymbol::Toyopa, new BigDecimal('0.40')),
            new PortfolioItem(StockSymbol::Somy, new BigDecimal('0.60')),
        ]);
    }
}
