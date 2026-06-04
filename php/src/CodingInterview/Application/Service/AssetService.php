<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Service;

use Folio\CodingInterview\Domain\Account;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\Stock;

final class AssetService
{
    /** @param array<string,BigDecimal> $prices */
    public static function evaluateStock(Stock $stock, array $prices): BigDecimal
    {
        if (!array_key_exists($stock->symbol->value, $prices)) {
            throw new \LogicException("missing price for {$stock->symbol->value}");
        }
        return $stock->qty->mul($prices[$stock->symbol->value]);
    }

    /** @param array<string,BigDecimal> $prices */
    public static function totalValuation(Account $account, array $prices): BigDecimal
    {
        $sum = BigDecimal::zero();
        foreach ($account->stocks as $e) {
            $sum = $sum->add(self::evaluateStock($e, $prices));
        }
        return $sum->add($account->cash);
    }
}
