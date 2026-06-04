package folio.codinginterview;

import folio.codinginterview.infrastructure.server.DummyServer;
import folio.codinginterview.presentation.PortfolioController;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OptimalPortfolioScenarioTest {

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertTrue(expected.compareTo(actual) == 0, "expected " + expected + " but was " + actual);
    }

    @Test
    void 最適ポートフォリオを更新取得できる() throws Exception {
        DummyServer server = DummyServer.defaultInstance();
        PortfolioController pc = server.portfolioController();

        // Given: 最適ポートフォリオを Toyopa=0.20, Somy=0.80 に更新する
        pc.updateOptimalPortfolio(new PortfolioController.UpdateOptimalPortfolioRequest(Arrays.asList(
                new PortfolioController.PortfolioItemDto("Toyopa", "0.20"),
                new PortfolioController.PortfolioItemDto("Somy", "0.80")
        ))).get();

        // When: 最適ポートフォリオを取得する
        PortfolioController.GetOptimalPortfolioResponse first = pc.getOptimalPortfolio().get();
        Map<String, String> firstMap = new HashMap<>();
        for (PortfolioController.PortfolioItemDto p : first.portfolios()) {
            firstMap.put(p.symbol(), p.rate());
        }

        // Then: Toyopa=0.20, Somy=0.80 が返される
        assertBigDecimalEquals(new BigDecimal("0.20"), new BigDecimal(firstMap.get("Toyopa")));
        assertBigDecimalEquals(new BigDecimal("0.80"), new BigDecimal(firstMap.get("Somy")));

        // When: 最適ポートフォリオを Toyopa=0.40, Somy=0.60 に更新して再取得する
        pc.updateOptimalPortfolio(new PortfolioController.UpdateOptimalPortfolioRequest(Arrays.asList(
                new PortfolioController.PortfolioItemDto("Toyopa", "0.40"),
                new PortfolioController.PortfolioItemDto("Somy", "0.60")
        ))).get();
        PortfolioController.GetOptimalPortfolioResponse second = pc.getOptimalPortfolio().get();
        Map<String, String> secondMap = new HashMap<>();
        for (PortfolioController.PortfolioItemDto p : second.portfolios()) {
            secondMap.put(p.symbol(), p.rate());
        }

        // Then: Toyopa=0.40, Somy=0.60 が返される
        assertBigDecimalEquals(new BigDecimal("0.40"), new BigDecimal(secondMap.get("Toyopa")));
        assertBigDecimalEquals(new BigDecimal("0.60"), new BigDecimal(secondMap.get("Somy")));
    }
}
