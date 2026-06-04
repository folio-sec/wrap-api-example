<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Repository;

use Folio\CodingInterview\Domain\Account;
use Folio\CodingInterview\Domain\UserId;

/**
 * 口座管理リポジトリ。
 */
interface AccountRepository
{
    public function find(UserId $userId): ?Account;

    public function upsert(UserId $userId, Account $account): void;

    public function exists(UserId $userId): bool;
}
