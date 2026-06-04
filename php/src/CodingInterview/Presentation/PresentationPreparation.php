<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Presentation;

use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Domain\UserId;

trait PresentationPreparation
{
    protected function parseUserId(string $s): UserId
    {
        try {
            return new UserId($s);
        } catch (\Throwable $e) {
            throw new BadRequestException($e->getMessage());
        }
    }

    protected function parseAmount(string $s): BigDecimal
    {
        try {
            return new BigDecimal($s);
        } catch (\Throwable $e) {
            throw new BadRequestException("invalid amount: {$s}");
        }
    }
}
