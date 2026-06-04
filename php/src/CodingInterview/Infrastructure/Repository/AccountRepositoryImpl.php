<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Infrastructure\Repository;

use Folio\CodingInterview\Application\Repository\AccountRepository;
use Folio\CodingInterview\Domain\Account;
use Folio\CodingInterview\Domain\UserId;

final class AccountRepositoryImpl implements AccountRepository
{
    /** @var array<string,Account> */
    private array $store = [];

    public function find(UserId $userId): ?Account
    {
        return $this->store[$userId->value] ?? null;
    }

    public function upsert(UserId $userId, Account $account): void
    {
        $this->store[$userId->value] = $account;
    }

    public function exists(UserId $userId): bool
    {
        return array_key_exists($userId->value, $this->store);
    }
}
