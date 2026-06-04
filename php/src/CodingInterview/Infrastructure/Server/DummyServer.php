<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Infrastructure\Server;

use Folio\CodingInterview\Application\Usecase\Asset\GetAssetUsecase;
use Folio\CodingInterview\Application\Usecase\MarketPrice\UpdateMarketPriceUsecase;
use Folio\CodingInterview\Application\Usecase\Order\AdditionalBuyOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\NewContributionOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Order\RebalanceOrderUsecase;
use Folio\CodingInterview\Application\Usecase\Portfolio\GetLatestPortfolioUsecase;
use Folio\CodingInterview\Application\Usecase\Portfolio\UpdatePortfolioUsecase;
use Folio\CodingInterview\Infrastructure\Repository\AccountRepositoryImpl;
use Folio\CodingInterview\Infrastructure\Repository\MarketPriceRepositoryImpl;
use Folio\CodingInterview\Infrastructure\Repository\PortfolioRepositoryImpl;
use Folio\CodingInterview\Presentation\AssetController;
use Folio\CodingInterview\Presentation\MarketPriceController;
use Folio\CodingInterview\Presentation\OrderController;
use Folio\CodingInterview\Presentation\PortfolioController;

final class DummyServer
{
    public function __construct(
        public readonly AssetController $assetController,
        public readonly PortfolioController $portfolioController,
        public readonly OrderController $orderController,
        public readonly MarketPriceController $marketPriceController,
    ) {}

    public static function default(): DummyServer
    {
        $portfolioRepository = new PortfolioRepositoryImpl();
        $accountRepository = new AccountRepositoryImpl();
        $marketPriceRepository = new MarketPriceRepositoryImpl();

        $getAssetUsecase = new GetAssetUsecase($accountRepository, $marketPriceRepository);
        $getLatestPortfolioUsecase = new GetLatestPortfolioUsecase($portfolioRepository);
        $updatePortfolioUsecase = new UpdatePortfolioUsecase($portfolioRepository);
        $updateMarketPriceUsecase = new UpdateMarketPriceUsecase($marketPriceRepository);
        $newContributionOrderUsecase = new NewContributionOrderUsecase(
            $accountRepository, $portfolioRepository, $marketPriceRepository,
        );
        $additionalBuyOrderUsecase = new AdditionalBuyOrderUsecase(
            $accountRepository, $portfolioRepository, $marketPriceRepository,
        );
        $rebalanceOrderUsecase = new RebalanceOrderUsecase(
            $accountRepository, $portfolioRepository, $marketPriceRepository,
        );

        return new DummyServer(
            new AssetController($getAssetUsecase),
            new PortfolioController($getLatestPortfolioUsecase, $updatePortfolioUsecase),
            new OrderController($newContributionOrderUsecase, $additionalBuyOrderUsecase, $rebalanceOrderUsecase),
            new MarketPriceController($updateMarketPriceUsecase),
        );
    }
}
