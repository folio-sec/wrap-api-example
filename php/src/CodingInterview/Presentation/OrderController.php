<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Presentation;

use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderAmountTooSmallException;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUserNotFoundException;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUsecaseInput;
use Folio\CodingInterview\Application\Usecase\Order\NewOrderAmountTooSmallException;
use Folio\CodingInterview\Application\Usecase\Order\NewOrderUserAlreadyExistsException;
use Folio\CodingInterview\Application\Usecase\Order\NewOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\NewOrderUsecaseInput;

final class NewOrderRequest
{
    public function __construct(
        public readonly string $userId,
        public readonly string $amount,
    ) {}
}

final class AdditionalOrderRequest
{
    public function __construct(
        public readonly string $userId,
        public readonly string $amount,
    ) {}
}

final class OrderController
{
    use PresentationPreparation;

    public function __construct(
        private readonly NewOrderUsecase $newOrderUsecase,
        private readonly AdditionalBuyOrderUsecase $additionalBuyOrderUsecase,
    ) {}

    public function newOrder(NewOrderRequest $req): void
    {
        $uid = $this->parseUserId($req->userId);
        $amt = $this->parseAmount($req->amount);
        try {
            $this->newOrderUsecase->run(new NewOrderUsecaseInput($uid, $amt));
        } catch (NewOrderUserAlreadyExistsException $e) {
            throw new BadRequestException('user already has account');
        } catch (NewOrderAmountTooSmallException $e) {
            throw new BadRequestException('amount is too small');
        }
    }

    public function additionalOrder(AdditionalOrderRequest $req): void
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

}
