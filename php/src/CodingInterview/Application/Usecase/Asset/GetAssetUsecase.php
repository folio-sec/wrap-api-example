<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Asset;

use Folio\CodingInterview\Application\Repository\AccountRepository;
use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\StockSymbol;
use Folio\CodingInterview\Domain\UserId;

final class GetAssetUsecaseInput
{
    public function __construct(public readonly UserId $userId) {}
}

final class GetAssetStockOutput
{
    public function __construct(
        public readonly StockSymbol $symbol,
        public readonly BigDecimal $amountJpy,
    ) {}
}

final class GetAssetUsecaseOutput
{
    /** @param GetAssetStockOutput[] $stocks */
    public function __construct(
        public readonly BigDecimal $cashAmount,
        public readonly array $stocks,
    ) {}
}

final class GetAssetUsecase
{
    public function __construct(
        private readonly AccountRepository $accountRepository,
    ) {}

    public function run(GetAssetUsecaseInput $input): GetAssetUsecaseOutput
    {
        $account = $this->accountRepository->find($input->userId);
        if ($account === null) {
            throw new GetAssetUsecaseUserNotFoundException();
        }
        $stocks = [];
        foreach ($account->stocks as $e) {
            $stocks[] = new GetAssetStockOutput($e->symbol, $e->amountJpy);
        }
        return new GetAssetUsecaseOutput($account->cash, $stocks);
    }
}
