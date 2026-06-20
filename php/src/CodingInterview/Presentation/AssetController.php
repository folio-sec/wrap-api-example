<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Presentation;

use Folio\CodingInterview\Application\Usecase\Asset\GetAssetUsecase;
use Folio\CodingInterview\Application\Usecase\Asset\GetAssetUsecaseInput;
use Folio\CodingInterview\Application\Usecase\Asset\GetAssetUsecaseUserNotFoundException;

final class GetAssetRequest
{
    public function __construct(public readonly string $userId) {}
}

final class GetAssetStockDto
{
    public function __construct(
        public readonly string $symbol,
        public readonly string $amountJpy,
    ) {}
}

final class GetAssetResponse
{
    /** @param GetAssetStockDto[] $stocks */
    public function __construct(
        public readonly string $cashAmount,
        public readonly array $stocks,
    ) {}
}

final class AssetController
{
    use PresentationPreparation;

    public function __construct(private readonly GetAssetUsecase $getAssetUsecase) {}

    public function getAsset(GetAssetRequest $req): GetAssetResponse
    {
        $uid = $this->parseUserId($req->userId);
        try {
            $out = $this->getAssetUsecase->run(new GetAssetUsecaseInput($uid));
        } catch (GetAssetUsecaseUserNotFoundException $e) {
            throw new BadRequestException('user not found');
        }
        $stocks = [];
        foreach ($out->stocks as $e) {
            $stocks[] = new GetAssetStockDto($e->symbol->value, $e->amountJpy->toString());
        }
        return new GetAssetResponse($out->cashAmount->toString(), $stocks);
    }
}
