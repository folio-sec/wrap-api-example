<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Application\Usecase\Asset;

final class GetAssetUsecaseUserNotFoundException extends \RuntimeException
{
    public function __construct()
    {
        parent::__construct('user not found');
    }
}
