<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Repository;

use Folio\CodingInterview\Domain\Portfolio;

/**
 * 最適ポートフォリオリポジトリ。
 */
interface PortfolioRepository
{
    public function get(): Portfolio;

    public function update(Portfolio $portfolio): void;
}
