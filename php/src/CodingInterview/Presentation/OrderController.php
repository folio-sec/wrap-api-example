<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Presentation;

use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderAmountTooSmallException;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUserNotFoundException;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUsecaseInput;
use Folio\CodingInterview\Application\Usecase\Order\NewContributionOrderAmountTooSmallException;
use Folio\CodingInterview\Application\Usecase\Order\NewContributionOrderUserAlreadyExistsException;
use Folio\CodingInterview\Application\Usecase\Order\NewContributionOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\NewContributionOrderUsecaseInput;
use Folio\CodingInterview\Application\Usecase\Order\RebalanceOrderUserNotFoundException;
use Folio\CodingInterview\Application\Usecase\Order\RebalanceOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\RebalanceOrderUsecaseInput;

final class NewContributionOrderRequest
{
    public function __construct(
        public readonly string $userId,
        public readonly string $amount,
    ) {}
}

final class AdditionalContributionOrderRequest
{
    public function __construct(
        public readonly string $userId,
        public readonly string $amount,
    ) {}
}

final class RebalanceOrderRequest
{
    public function __construct(public readonly string $userId) {}
}

final class OrderController
{
    use PresentationPreparation;

    public function __construct(
        private readonly NewContributionOrderUsecase $newContributionOrderUsecase,
        private readonly AdditionalBuyOrderUsecase $additionalBuyOrderUsecase,
        private readonly RebalanceOrderUsecase $rebalanceOrderUsecase,
    ) {}

    public function newContributionOrder(NewContributionOrderRequest $req): void
    {
        $uid = $this->parseUserId($req->userId);
        $amt = $this->parseAmount($req->amount);
        try {
            $this->newContributionOrderUsecase->run(new NewContributionOrderUsecaseInput($uid, $amt));
        } catch (NewContributionOrderUserAlreadyExistsException $e) {
            throw new BadRequestException('user already has account');
        } catch (NewContributionOrderAmountTooSmallException $e) {
            throw new BadRequestException('amount is too small');
        }
    }

    public function additionalContributionOrder(AdditionalContributionOrderRequest $req): void
    {
        $uid = $this->parseUserId($req->userId);
        $amt = $this->parseAmount($req->amount);
        try {
            $this->additionalBuyOrderUsecase->run(new AdditionalBuyOrderUsecaseInput($uid, $amt));
        } catch (AdditionalBuyOrderUserNotFoundException $e) {
            throw new BadRequestException('user has no live account');
        } catch (AdditionalBuyOrderAmountTooSmallException $e) {
            throw new BadRequestException('amount is too small');
        }
    }

    public function rebalanceOrder(RebalanceOrderRequest $req): void
    {
        $uid = $this->parseUserId($req->userId);
        try {
            $this->rebalanceOrderUsecase->run(new RebalanceOrderUsecaseInput($uid));
        } catch (RebalanceOrderUserNotFoundException $e) {
            throw new BadRequestException('user has no live account');
        }
    }
}
