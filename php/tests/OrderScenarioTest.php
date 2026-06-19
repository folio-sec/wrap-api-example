<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Tests;

use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Infrastructure\Server\DummyServer;
use Folio\CodingInterview\Presentation\AdditionalOrderRequest;
use Folio\CodingInterview\Presentation\BadRequestException;
use Folio\CodingInterview\Presentation\GetAssetRequest;
use Folio\CodingInterview\Presentation\NewOrderRequest;
use Folio\CodingInterview\Presentation\PortfolioItemDto;
use Folio\CodingInterview\Presentation\RebalanceOrderRequest;
use Folio\CodingInterview\Presentation\UpdateOptimalPortfolioRequest;
use PHPUnit\Framework\TestCase;

final class OrderScenarioTest extends TestCase
{
    public function test新規拠出追加拠出リバランスの一連の操作が正しく機能する(): void
    {
        $server = DummyServer::default();
        $ac = $server->assetController;
        $pc = $server->portfolioController;
        $oc = $server->orderController;

        // initialize optimal portfolio
        $pc->updateOptimalPortfolio(new UpdateOptimalPortfolioRequest([
            new PortfolioItemDto('Toyopa', '0.40'),
            new PortfolioItemDto('Somy', '0.60'),
        ]));

        $userId = bin2hex(random_bytes(8));

        // Given: 存在しないユーザーで資産を取得しようとする
        try {
            $ac->getAsset(new GetAssetRequest($userId));
            $this->fail('expected BadRequestException for unknown user, but no exception was thrown');
        } catch (BadRequestException $e) {
            // Then: BadRequestException が返される
        }

        // When: 最適ポートフォリオを Toyopa=40%, Somy=60% に更新する
        $pc->updateOptimalPortfolio(new UpdateOptimalPortfolioRequest([
            new PortfolioItemDto('Toyopa', '0.40'),
            new PortfolioItemDto('Somy', '0.60'),
        ]));

        // And: 新規注文を 100,000 円で注文する
        $oc->newOrder(new NewOrderRequest($userId, '100000'));

        $asset1 = $ac->getAsset(new GetAssetRequest($userId));
        $symbols = array_map(fn($e) => $e->symbol, $asset1->stocks);
        sort($symbols);
        $this->assertSame(['Somy', 'Toyopa'], $symbols);

        $total1 = new BigDecimal($asset1->cashAmount);
        foreach ($asset1->stocks as $e) {
            $total1 = $total1->add(new BigDecimal($e->amountJpy));
        }
        $this->assertLessThanOrEqual(0, $total1->sub(new BigDecimal('100000'))->abs()->compare(new BigDecimal('2')), "total1={$total1} (expected ≈100000)");

        // Then: 現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の評価額が 38,000 円(40%)、Somy の評価額が 57,000 円(60%) となる
        $toyopa1 = $this->findStock($asset1->stocks, 'Toyopa');
        $somy1 = $this->findStock($asset1->stocks, 'Somy');
        $this->assertSame('38000', (string)(new BigDecimal($toyopa1->amountJpy)));
        $this->assertSame('57000', (string)(new BigDecimal($somy1->amountJpy)));
        $this->assertSame('5000', (string)(new BigDecimal($asset1->cashAmount)));

        // When: 追加注文を 100,000 円で注文する
        $oc->additionalOrder(new AdditionalOrderRequest($userId, '100000'));

        // Then: 資産合計が約 200,000 円になる
        $asset2 = $ac->getAsset(new GetAssetRequest($userId));
        $total2 = new BigDecimal($asset2->cashAmount);
        foreach ($asset2->stocks as $e) {
            $total2 = $total2->add(new BigDecimal($e->amountJpy));
        }
        $this->assertLessThanOrEqual(0, $total2->sub(new BigDecimal('200000'))->abs()->compare(new BigDecimal('4')), "total2={$total2} (expected ≈200000)");

        // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 76,000 円(40%)、Somy の評価額が 114,000 円(60%) となる
        $toyopa2 = $this->findStock($asset2->stocks, 'Toyopa');
        $somy2 = $this->findStock($asset2->stocks, 'Somy');
        $this->assertSame('76000', (string)(new BigDecimal($toyopa2->amountJpy)));
        $this->assertSame('114000', (string)(new BigDecimal($somy2->amountJpy)));
        $this->assertSame('10000', (string)(new BigDecimal($asset2->cashAmount)));

        // When: 最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする
        $pc->updateOptimalPortfolio(new UpdateOptimalPortfolioRequest([
            new PortfolioItemDto('Toyopa', '0.10'),
            new PortfolioItemDto('Somy', '0.90'),
        ]));
        $oc->rebalanceOrder(new RebalanceOrderRequest($userId));

        // Then: リバランス後も資産合計がほぼ変わらない
        $asset3 = $ac->getAsset(new GetAssetRequest($userId));
        $total3 = new BigDecimal($asset3->cashAmount);
        foreach ($asset3->stocks as $e) {
            $total3 = $total3->add(new BigDecimal($e->amountJpy));
        }
        $this->assertLessThanOrEqual(0, $total3->sub($total2)->abs()->compare(new BigDecimal('4')), "total3={$total3}, total2={$total2} (expected ≈ equal)");

        // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 19,000 円(10%)、Somy の評価額が 171,000 円(90%) となる
        $toyopa3 = $this->findStock($asset3->stocks, 'Toyopa');
        $somy3 = $this->findStock($asset3->stocks, 'Somy');
        $this->assertSame('19000', (string)(new BigDecimal($toyopa3->amountJpy)));
        $this->assertSame('171000', (string)(new BigDecimal($somy3->amountJpy)));
        $this->assertSame('10000', (string)(new BigDecimal($asset3->cashAmount)));
    }

    /** @param array $stocks */
    private function findStock(array $stocks, string $symbol)
    {
        foreach ($stocks as $e) {
            if ($e->symbol === $symbol) {
                return $e;
            }
        }
        $this->fail("stock not found: {$symbol}");
    }
}
