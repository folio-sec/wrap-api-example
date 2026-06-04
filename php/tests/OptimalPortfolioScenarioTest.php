<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Tests;

use Folio\CodingInterview\Domain\BigDecimal;
use Folio\CodingInterview\Infrastructure\Server\DummyServer;
use Folio\CodingInterview\Presentation\PortfolioItemDto;
use Folio\CodingInterview\Presentation\UpdateOptimalPortfolioRequest;
use PHPUnit\Framework\TestCase;

final class OptimalPortfolioScenarioTest extends TestCase
{
    public function test最適ポートフォリオを更新取得できる(): void
    {
        $server = DummyServer::default();
        $pc = $server->portfolioController;

        $pc->updateOptimalPortfolio(new UpdateOptimalPortfolioRequest([
            new PortfolioItemDto('Toyopa', '0.20'),
            new PortfolioItemDto('Somy', '0.80'),
        ]));

        $first = $pc->getOptimalPortfolio();
        $firstMap = [];
        foreach ($first->portfolios as $p) {
            $firstMap[$p->symbol] = $p->rate;
        }
        $this->assertSame('0.2', (string)(new BigDecimal($firstMap['Toyopa'])));
        $this->assertSame('0.8', (string)(new BigDecimal($firstMap['Somy'])));

        $pc->updateOptimalPortfolio(new UpdateOptimalPortfolioRequest([
            new PortfolioItemDto('Toyopa', '0.40'),
            new PortfolioItemDto('Somy', '0.60'),
        ]));
        $second = $pc->getOptimalPortfolio();
        $secondMap = [];
        foreach ($second->portfolios as $p) {
            $secondMap[$p->symbol] = $p->rate;
        }
        $this->assertSame('0.4', (string)(new BigDecimal($secondMap['Toyopa'])));
        $this->assertSame('0.6', (string)(new BigDecimal($secondMap['Somy'])));
    }
}
