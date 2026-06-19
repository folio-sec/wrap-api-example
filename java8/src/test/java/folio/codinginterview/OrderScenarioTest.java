package folio.codinginterview;

import folio.codinginterview.infrastructure.server.DummyServer;
import folio.codinginterview.presentation.AssetController;
import folio.codinginterview.presentation.OrderController;
import folio.codinginterview.presentation.PortfolioController;
import folio.codinginterview.presentation.PresentationException.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OrderScenarioTest {

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertTrue(expected.compareTo(actual) == 0, "expected " + expected + " but was " + actual);
    }

    private final DummyServer server = DummyServer.defaultInstance();
    private final AssetController ac = server.assetController();
    private final PortfolioController pc = server.portfolioController();
    private final OrderController oc = server.orderController();

    @BeforeEach
    void setUp() throws Exception {
        // initialize optimal portfolio
        pc.updateOptimalPortfolio(new PortfolioController.UpdateOptimalPortfolioRequest(Arrays.asList(
                new PortfolioController.PortfolioItemDto("Toyopa", "0.40"),
                new PortfolioController.PortfolioItemDto("Somy", "0.60")
        ))).get();
    }

    private Throwable unwrap(Throwable e) {
        while (e instanceof ExecutionException || e instanceof CompletionException) {
            if (e.getCause() == null) break;
            e = e.getCause();
        }
        return e;
    }

    @Test
    void 新規注文追加注文リバランスの一連の操作が正しく機能する() throws Exception {
        String userId = UUID.randomUUID().toString();

        // Given: 存在しないユーザーで資産を取得しようとする
        Throwable notFound = null;
        try {
            ac.getAsset(new AssetController.GetAssetRequest(userId)).get();
            fail("expected exception");
        } catch (Exception e) {
            notFound = unwrap(e);
        }
        // Then: BadRequestException が返される
        assertTrue(notFound instanceof BadRequestException);

        // When: 最適ポートフォリオを Toyopa=40%, Somy=60% に更新する
        pc.updateOptimalPortfolio(new PortfolioController.UpdateOptimalPortfolioRequest(Arrays.asList(
                new PortfolioController.PortfolioItemDto("Toyopa", "0.40"),
                new PortfolioController.PortfolioItemDto("Somy", "0.60")
        ))).get();

        // And: 新規注文を 100,000 円で注文する
        oc.newOrder(new OrderController.NewOrderRequest(userId, "100000")).get();

        AssetController.GetAssetResponse asset1 = ac.getAsset(new AssetController.GetAssetRequest(userId)).get();
        List<String> symbols1 = asset1.stocks().stream().map(AssetController.StockDto::symbol).collect(Collectors.toList());
        assertTrue(symbols1.contains("Toyopa") && symbols1.contains("Somy") && symbols1.size() == 2);
        BigDecimal total1 = new BigDecimal(asset1.cashAmount());
        for (AssetController.StockDto e : asset1.stocks()) {
            total1 = total1.add(new BigDecimal(e.amountJpy()));
        }
        assertTrue(total1.subtract(new BigDecimal(100000)).abs().compareTo(new BigDecimal(2)) <= 0);

        // Then: 現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の保有額が 38,000 円(40%)、Somy の保有額が 57,000 円(60%) となる
        AssetController.StockDto asset1Toyopa = asset1.stocks().stream().filter(e -> e.symbol().equals("Toyopa")).findFirst().get();
        AssetController.StockDto asset1Somy = asset1.stocks().stream().filter(e -> e.symbol().equals("Somy")).findFirst().get();
        assertBigDecimalEquals(new BigDecimal("38000"), new BigDecimal(asset1Toyopa.amountJpy()));
        assertBigDecimalEquals(new BigDecimal("57000"), new BigDecimal(asset1Somy.amountJpy()));
        assertBigDecimalEquals(new BigDecimal("5000"), new BigDecimal(asset1.cashAmount()));

        // When: 追加注文を 100,000 円で注文する
        oc.additionalOrder(new OrderController.AdditionalOrderRequest(userId, "100000")).get();

        // Then: 資産合計が約 200,000 円になる
        AssetController.GetAssetResponse asset2 = ac.getAsset(new AssetController.GetAssetRequest(userId)).get();
        BigDecimal total2 = new BigDecimal(asset2.cashAmount());
        for (AssetController.StockDto e : asset2.stocks()) {
            total2 = total2.add(new BigDecimal(e.amountJpy()));
        }
        assertTrue(total2.subtract(new BigDecimal(200000)).abs().compareTo(new BigDecimal(4)) <= 0);

        // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の保有額が 76,000 円(40%)、Somy の保有額が 114,000 円(60%) となる
        AssetController.StockDto asset2Toyopa = asset2.stocks().stream().filter(e -> e.symbol().equals("Toyopa")).findFirst().get();
        AssetController.StockDto asset2Somy = asset2.stocks().stream().filter(e -> e.symbol().equals("Somy")).findFirst().get();
        assertBigDecimalEquals(new BigDecimal("76000"), new BigDecimal(asset2Toyopa.amountJpy()));
        assertBigDecimalEquals(new BigDecimal("114000"), new BigDecimal(asset2Somy.amountJpy()));
        assertBigDecimalEquals(new BigDecimal("10000"), new BigDecimal(asset2.cashAmount()));

        // When: 最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする
        pc.updateOptimalPortfolio(new PortfolioController.UpdateOptimalPortfolioRequest(Arrays.asList(
                new PortfolioController.PortfolioItemDto("Toyopa", "0.10"),
                new PortfolioController.PortfolioItemDto("Somy", "0.90")
        ))).get();
        oc.rebalanceOrder(new OrderController.RebalanceOrderRequest(userId)).get();

        // Then: リバランス後も資産合計がほぼ変わらない
        AssetController.GetAssetResponse asset3 = ac.getAsset(new AssetController.GetAssetRequest(userId)).get();
        BigDecimal total3 = new BigDecimal(asset3.cashAmount());
        for (AssetController.StockDto e : asset3.stocks()) {
            total3 = total3.add(new BigDecimal(e.amountJpy()));
        }
        assertTrue(total3.subtract(total2).abs().compareTo(new BigDecimal(4)) <= 0);

        // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の保有額が 19,000 円(10%)、Somy の保有額が 171,000 円(90%) となる
        AssetController.StockDto asset3Toyopa = asset3.stocks().stream().filter(e -> e.symbol().equals("Toyopa")).findFirst().get();
        AssetController.StockDto asset3Somy = asset3.stocks().stream().filter(e -> e.symbol().equals("Somy")).findFirst().get();
        assertBigDecimalEquals(new BigDecimal("19000"), new BigDecimal(asset3Toyopa.amountJpy()));
        assertBigDecimalEquals(new BigDecimal("171000"), new BigDecimal(asset3Somy.amountJpy()));
        assertBigDecimalEquals(new BigDecimal("10000"), new BigDecimal(asset3.cashAmount()));
    }
}
