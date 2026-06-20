<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Infrastructure\Server;

use Folio\CodingInterview\Application\Usecase\Asset\GetAssetUsecase;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\NewOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\RebalanceOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Portfolio\GetLatestPortfolioUsecase;
use Folio\CodingInterview\Application\Usecase\Portfolio\UpdatePortfolioUsecase;
use Folio\CodingInterview\Infrastructure\Repository\AccountRepositoryImpl;
use Folio\CodingInterview\Infrastructure\Repository\PortfolioRepositoryImpl;
use Folio\CodingInterview\Presentation\AssetController;
use Folio\CodingInterview\Presentation\OrderController;
use Folio\CodingInterview\Presentation\PortfolioController;

final class DummyServer
{
    public function __construct(
        public readonly AssetController $assetController,
        public readonly PortfolioController $portfolioController,
        public readonly OrderController $orderController,
    ) {}

    public static function default(): DummyServer
    {
        $portfolioRepository = new PortfolioRepositoryImpl();
        $accountRepository = new AccountRepositoryImpl();

        $getAssetUsecase = new GetAssetUsecase($accountRepository);
        $getLatestPortfolioUsecase = new GetLatestPortfolioUsecase($portfolioRepository);
        $updatePortfolioUsecase = new UpdatePortfolioUsecase($portfolioRepository);
        $newOrderUsecase = new NewOrderUsecase($accountRepository, $portfolioRepository);
        $additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase($accountRepository, $portfolioRepository);
        $rebalanceOrderUsecase = new RebalanceOrderUsecase($accountRepository, $portfolioRepository);

        return new DummyServer(
            new AssetController($getAssetUsecase),
            new PortfolioController($getLatestPortfolioUsecase, $updatePortfolioUsecase),
            new OrderController($newOrderUsecase, $additionalBuyOrderUsecase, $rebalanceOrderUsecase),
        );
    }
}
