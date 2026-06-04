<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Order;

use Folio\CodingInterview\Application\Repository\AccountRepository;
use Folio\CodingInterview\Application\Repository\MarketPriceRepository;
use Folio\CodingInterview\Application\Repository\PortfolioRepository;
use Folio\CodingInterview\Application\Service\PortfolioService;
use Folio\CodingInterview\Domain\AppConstants;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\UserId;

final class AdditionalBuyOrderUsecaseInput
{
    public function __construct(
        public readonly UserId $userId,
        public readonly BigDecimal $amount,
    ) {}
}

final class AdditionalBuyOrderUserNotFoundException extends \RuntimeException
{
    public function __construct() { parent::__construct('user has no live account'); }
}

final class AdditionalBuyOrderAmountTooSmallException extends \RuntimeException
{
    public function __construct() { parent::__construct('amount is too small'); }
}

final class AdditionalBuyOrderUsecase
{
    public function __construct(
        private readonly AccountRepository $accountRepository,
        private readonly PortfolioRepository $portfolioRepository,
        private readonly MarketPriceRepository $marketPriceRepository,
    ) {}

    public function run(AdditionalBuyOrderUsecaseInput $input): void
    {
        if ($input->amount->lt(AppConstants::minOperationAmount())) {
            throw new AdditionalBuyOrderAmountTooSmallException();
        }
        $account = $this->accountRepository->find($input->userId);
        if ($account === null) {
            throw new AdditionalBuyOrderUserNotFoundException();
        }
        $portfolio = $this->portfolioRepository->get();
        $prices = $this->marketPriceRepository->all();
        $updated = PortfolioService::allocateAdditional($account, $input->amount, $portfolio, $prices);
        $this->accountRepository->upsert($input->userId, $updated);
    }
}
