<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Order;

use Folio\CodingInterview\Application\Repository\AccountRepository;
use Folio\CodingInterview\Application\Repository\MarketPriceRepository;
use Folio\CodingInterview\Application\Repository\PortfolioRepository;
use Folio\CodingInterview\Application\Service\PortfolioService;
use Folio\CodingInterview\Domain\UserId;

final class RebalanceOrderUsecaseInput
{
    public function __construct(public readonly UserId $userId) {}
}

final class RebalanceOrderUserNotFoundException extends \RuntimeException
{
    public function __construct() { parent::__construct('user has no live account'); }
}

final class RebalanceOrderUsecase
{
    public function __construct(
        private readonly AccountRepository $accountRepository,
        private readonly PortfolioRepository $portfolioRepository,
        private readonly MarketPriceRepository $marketPriceRepository,
    ) {}

    public function run(RebalanceOrderUsecaseInput $input): void
    {
        $account = $this->accountRepository->find($input->userId);
        if ($account === null) {
            throw new RebalanceOrderUserNotFoundException();
        }
        $portfolio = $this->portfolioRepository->get();
        $prices = $this->marketPriceRepository->all();
        $updated = PortfolioService::rebalance($account, $portfolio, $prices);
        $this->accountRepository->upsert($input->userId, $updated);
    }
}
