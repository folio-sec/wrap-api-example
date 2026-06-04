<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Service;

use Folio\CodingInterview\Domain\Account;
use Folio\CodingInterview\Domain\AppConstants;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\Stock;
use Folio\CodingInterview\Domain\StockSymbol;
use Folio\CodingInterview\Domain\Portfolio;

final class PortfolioService
{
    private static function floor2(BigDecimal $x): BigDecimal
    {
        return $x->setScaleDown(2);
    }

    private static function floor0(BigDecimal $x): BigDecimal
    {
        return $x->setScaleDown(0);
    }

    /** @param array<string,BigDecimal> $prices */
    private static function priceOf(array $prices, StockSymbol $symbol): BigDecimal
    {
        if (!array_key_exists($symbol->value, $prices)) {
            throw new \LogicException("missing price for {$symbol->value}");
        }
        return $prices[$symbol->value];
    }

    /**
     * Allocate a brand-new account given a contribution amount.
     * @param array<string,BigDecimal> $prices
     */
    public static function allocateNew(BigDecimal $amount, Portfolio $portfolio, array $prices): Account
    {
        $cashFromRate = self::floor0($amount->mul(AppConstants::cashRate()));
        $investable = $amount->sub($cashFromRate);
        $stocks = [];
        foreach ($portfolio->items as $item) {
            $price = self::priceOf($prices, $item->symbol);
            $qty = self::floor2($investable->mul($item->rate)->div($price));
            $stocks[] = new Stock($item->symbol, $qty);
        }
        $usedForStocks = BigDecimal::zero();
        foreach ($stocks as $e) {
            $usedForStocks = $usedForStocks->add($e->qty->mul(self::priceOf($prices, $e->symbol)));
        }
        $residual = $investable->sub($usedForStocks);
        return new Account($cashFromRate->add($residual), $stocks);
    }

    /**
     * Additional contribution: only buy (no sell). Residual is kept in cash.
     * @param array<string,BigDecimal> $prices
     */
    public static function allocateAdditional(
        Account $account,
        BigDecimal $amount,
        Portfolio $portfolio,
        array $prices,
    ): Account {
        $totalAfter = AssetService::totalValuation($account, $prices)->add($amount);
        $targetCash = self::floor0($totalAfter->mul(AppConstants::cashRate()));
        $investable = $totalAfter->sub($targetCash);

        $currentQty = [];
        foreach ($account->stocks as $e) {
            $currentQty[$e->symbol->value] = $e->qty;
        }

        $portfolioSymbols = [];
        foreach ($portfolio->items as $i) {
            $portfolioSymbols[$i->symbol->value] = true;
        }
        $newPortfolioStocks = [];
        foreach ($portfolio->items as $item) {
            $price = self::priceOf($prices, $item->symbol);
            $targetQty = self::floor2($investable->mul($item->rate)->div($price));
            $current = $currentQty[$item->symbol->value] ?? BigDecimal::zero();
            $finalQty = $targetQty->gt($current) ? $targetQty : $current;
            $newPortfolioStocks[] = new Stock($item->symbol, $finalQty);
        }
        $preservedStocks = [];
        foreach ($account->stocks as $e) {
            if (!isset($portfolioSymbols[$e->symbol->value])) {
                $preservedStocks[] = $e;
            }
        }
        $allStocks = array_merge($newPortfolioStocks, $preservedStocks);

        $finalValuation = BigDecimal::zero();
        foreach ($allStocks as $e) {
            $finalValuation = $finalValuation->add($e->qty->mul(self::priceOf($prices, $e->symbol)));
        }
        $finalCash = $totalAfter->sub($finalValuation);
        return new Account($finalCash, $allStocks);
    }

    /**
     * Rebalance: re-allocate qty per portfolio target (buy and sell).
     * @param array<string,BigDecimal> $prices
     */
    public static function rebalance(Account $account, Portfolio $portfolio, array $prices): Account
    {
        // XXX this implementation might not be correct
        $investable = AssetService::totalValuation($account, $prices);
        $newStocks = [];
        foreach ($portfolio->items as $item) {
            $price = self::priceOf($prices, $item->symbol);
            $qty = self::floor2($investable->mul($item->rate)->div($price));
            $newStocks[] = new Stock($item->symbol, $qty);
        }
        $finalValuation = BigDecimal::zero();
        foreach ($newStocks as $e) {
            $finalValuation = $finalValuation->add($e->qty->mul(self::priceOf($prices, $e->symbol)));
        }
        $finalCash = $investable->sub($finalValuation);
        return new Account($finalCash, $newStocks);
    }
}
