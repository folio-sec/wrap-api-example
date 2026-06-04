<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

final class UserId
{
    public function __construct(public readonly string $value)
    {
        if ($value === '') {
            throw new \InvalidArgumentException('userId must not be empty');
        }
    }
}
