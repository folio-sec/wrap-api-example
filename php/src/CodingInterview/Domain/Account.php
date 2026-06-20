<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

/** 口座を表す。 */
final class Account
{
    /** @param Stock[] $stocks */
    public function __construct(
        public readonly BigDecimal $cash,
        public readonly array $stocks,
    ) {
    }

    /** 総資産（現金 + 各銘柄の保有額合計）を返す。 */
    public function total(): BigDecimal
    {
        $total = $this->cash;
        foreach ($this->stocks as $stock) {
            $total = $total->add($stock->amountJpy);
        }
        return $total;
    }

    /** 新規注文で口座を開設する。 */
    public static function openAccount(BigDecimal $amount, Portfolio $portfolio): self
    {
        $cashFromRate = $amount->mul(AppConstants::cashRate())->setScaleDown(0);
        $investable = $amount->sub($cashFromRate);
        $stocks = [];
        $usedForStocks = BigDecimal::zero();
        foreach ($portfolio->items as $item) {
            $amt = $investable->mul($item->rate)->setScaleDown(0);
            $stocks[] = new Stock($item->symbol, $amt);
            $usedForStocks = $usedForStocks->add($amt);
        }
        $residual = $investable->sub($usedForStocks);
        return new self($cashFromRate->add($residual), $stocks);
    }

    /** 追加注文で資金を追加する。 */
    public function addFunds(BigDecimal $amount, Portfolio $portfolio): self
    {
        $totalAfter = $this->total()->add($amount);
        $targetCash = $totalAfter->mul(AppConstants::cashRate())->setScaleDown(0);
        $investable = $totalAfter->sub($targetCash);

        $currentAmounts = [];
        foreach ($this->stocks as $e) {
            $currentAmounts[$e->symbol->value] = $e->amountJpy;
        }

        $portfolioSymbols = [];
        foreach ($portfolio->items as $i) {
            $portfolioSymbols[$i->symbol->value] = true;
        }

        $newPortfolioStocks = [];
        $usedForStocks = BigDecimal::zero();
        foreach ($portfolio->items as $item) {
            $target = $investable->mul($item->rate)->setScaleDown(0);
            $current = $currentAmounts[$item->symbol->value] ?? BigDecimal::zero();
            $final = $target->gt($current) ? $target : $current;
            $newPortfolioStocks[] = new Stock($item->symbol, $final);
            $usedForStocks = $usedForStocks->add($final);
        }

        $preservedStocks = [];
        foreach ($this->stocks as $e) {
            if (!isset($portfolioSymbols[$e->symbol->value])) {
                $preservedStocks[] = $e;
                $usedForStocks = $usedForStocks->add($e->amountJpy);
            }
        }

        $allStocks = array_merge($newPortfolioStocks, $preservedStocks);
        $finalCash = $totalAfter->sub($usedForStocks);
        return new self($finalCash, $allStocks);
    }

    /** リバランス注文で最適ポートフォリオに調整する。 */
    public function rebalance(Portfolio $portfolio): self
    {
        // XXX this implementation might not be correct
        $investable = $this->total();
        $stocks = [];
        $usedForStocks = BigDecimal::zero();
        foreach ($portfolio->items as $item) {
            $amt = $investable->mul($item->rate)->setScaleDown(0);
            $stocks[] = new Stock($item->symbol, $amt);
            $usedForStocks = $usedForStocks->add($amt);
        }
        $finalCash = $investable->sub($usedForStocks);
        return new self($finalCash, $stocks);
    }
}
