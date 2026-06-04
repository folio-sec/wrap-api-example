<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Infrastructure\Repository;

use Folio\CodingInterview\Application\Repository\PortfolioRepository;
use Folio\CodingInterview\Domain\AppConstants;
use Folio\CodingInterview\Domain\Portfolio;

final class PortfolioRepositoryImpl implements PortfolioRepository
{
    private Portfolio $portfolio;

    public function __construct()
    {
        $this->portfolio = AppConstants::initialPortfolio();
    }

    public function get(): Portfolio
    {
        return $this->portfolio;
    }

    public function update(Portfolio $portfolio): void
    {
        $this->portfolio = $portfolio;
    }
}
