<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Order;

use Folio\CodingInterview\Application\Repository\AccountRepository;
use Folio\CodingInterview\Application\Repository\PortfolioRepository;
use Folio\CodingInterview\Domain\Account;
use Folio\CodingInterview\Domain\AppConstants;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\UserId;

final class NewOrderUsecaseInput
{
    public function __construct(
        public readonly UserId $userId,
        public readonly BigDecimal $amount,
    ) {}
}

final class NewOrderUserAlreadyExistsException extends \RuntimeException
{
    public function __construct() { parent::__construct('user already has account'); }
}

final class NewOrderAmountTooSmallException extends \RuntimeException
{
    public function __construct() { parent::__construct('amount is too small'); }
}

final class NewOrderUsecase
{
    public function __construct(
        private readonly AccountRepository $accountRepository,
        private readonly PortfolioRepository $portfolioRepository,
    ) {}

    public function run(NewOrderUsecaseInput $input): void
    {
        if ($input->amount->lt(AppConstants::minOperationAmount())) {
            throw new NewOrderAmountTooSmallException();
        }
        if ($this->accountRepository->exists($input->userId)) {
            throw new NewOrderUserAlreadyExistsException();
        }
        $portfolio = $this->portfolioRepository->get();
        $account = Account::openAccount($input->amount, $portfolio);
        $this->accountRepository->upsert($input->userId, $account);
    }
}
