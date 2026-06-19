<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

/** 銘柄を表す。 */
enum StockSymbol: string
{
    case Toyopa = 'Toyopa';
    case Somy = 'Somy';

    public static function fromStringOrNull(string $s): ?StockSymbol
    {
        return match ($s) {
            'Toyopa' => self::Toyopa,
            'Somy' => self::Somy,
            default => null,
        };
    }
}
