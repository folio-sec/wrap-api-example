package folio.codinginterview

import folio.codinginterview.infrastructure.server.DummyServer
import folio.codinginterview.presentation.AssetController.GetAssetRequest
import folio.codinginterview.presentation.MarketPriceController.MarketPriceItemDto
import folio.codinginterview.presentation.MarketPriceController.UpdateMarketPriceRequest
import folio.codinginterview.presentation.OrderController.AdditionalContributionOrderRequest
import folio.codinginterview.presentation.OrderController.NewContributionOrderRequest
import folio.codinginterview.presentation.OrderController.RebalanceOrderRequest
import folio.codinginterview.presentation.PortfolioController.PortfolioItemDto
import folio.codinginterview.presentation.PortfolioController.UpdateOptimalPortfolioRequest
import folio.codinginterview.presentation.PresentationException.BadRequestException
import java.util.UUID
import org.scalatest.BeforeAndAfterEach
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.*

class OrderScenario extends AnyFeatureSpec with GivenWhenThen with BeforeAndAfterEach {
  given ExecutionContext = ExecutionContext.global

  extension [A](f: Future[A]) def await: A = Await.result(f, 5.seconds)

  val server = DummyServer.default()
  val ac = server.assetController
  val pc = server.portfolioController
  val oc = server.orderController
  val mp = server.marketPriceController

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    // initialize market price and optimal portfolio
    pc.updateOptimalPortfolio(
      UpdateOptimalPortfolioRequest(
        Seq(PortfolioItemDto("Toyopa", "0.40"), PortfolioItemDto("Somy", "0.60"))
      )
    ).await
    mp.updateMarketPrice(
      UpdateMarketPriceRequest(
        Seq(MarketPriceItemDto("Toyopa", "2.5"), MarketPriceItemDto("Somy", "3.0"))
      )
    ).await
  }

  Feature("Investment Operation") {
    Scenario("新規拠出・追加拠出・リバランスの一連の操作が正しく機能する") {
      val userId = UUID.randomUUID().toString

      Given("存在しないユーザーで資産を取得しようとする")
      val notFound = ac.getAsset(GetAssetRequest(userId)).failed.await

      Then("BadRequestException が返される")
      assertResult(true)(notFound.isInstanceOf[BadRequestException])

      When("最適ポートフォリオを Toyopa=40%, Somy=60% に更新する")
      pc.updateOptimalPortfolio(
        UpdateOptimalPortfolioRequest(
          Seq(PortfolioItemDto("Toyopa", "0.40"), PortfolioItemDto("Somy", "0.60"))
        )
      ).await

      And("新規拠出を 100,000 円で注文する")
      oc.newContributionOrder(NewContributionOrderRequest(userId, "100000")).await

      val asset1 = ac.getAsset(GetAssetRequest(userId)).await
      assertResult(Set("Toyopa", "Somy"))(asset1.stocks.map(_.symbol).toSet)
      val total1 = BigDecimal(asset1.cashAmount) + asset1.stocks.map(e => BigDecimal(e.evaluationAmount)).sum
      assertResult(true)((total1 - BigDecimal(100000)).abs <= BigDecimal(2))

      Then("現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の評価額が 38,000 円(40%)、Somy の評価額が 57,000 円(60%) となる")
      // investable = 100000 - floor0(100000 * 0.05) = 95000
      val asset1Toyopa = asset1.stocks.find(_.symbol == "Toyopa").get
      val asset1Somy = asset1.stocks.find(_.symbol == "Somy").get
      assertResult(BigDecimal("38000"))(
        BigDecimal(asset1Toyopa.evaluationAmount)
      ) // floor2(95000 * 0.40 / 2.5) = 15200株 * 2.5
      assertResult(BigDecimal("57000"))(
        BigDecimal(asset1Somy.evaluationAmount)
      ) // floor2(95000 * 0.60 / 3.0) = 19000株 * 3.0
      assertResult(BigDecimal("5000"))(BigDecimal(asset1.cashAmount)) // 100000 - 38000 - 57000

      When("追加拠出を 100,000 円で注文する")
      oc.additionalContributionOrder(AdditionalContributionOrderRequest(userId, "100000")).await

      Then("資産合計が約 200,000 円になる")
      val asset2 = ac.getAsset(GetAssetRequest(userId)).await
      val total2 = BigDecimal(asset2.cashAmount) + asset2.stocks.map(e => BigDecimal(e.evaluationAmount)).sum
      assertResult(true)((total2 - BigDecimal(200000)).abs <= BigDecimal(4))

      And("現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 76,000 円(40%)、Somy の評価額が 114,000 円(60%) となる")
      // totalAfter = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
      val asset2Toyopa = asset2.stocks.find(_.symbol == "Toyopa").get
      val asset2Somy = asset2.stocks.find(_.symbol == "Somy").get
      assertResult(BigDecimal("76000"))(
        BigDecimal(asset2Toyopa.evaluationAmount)
      ) // floor2(190000 * 0.40 / 2.5) = 30400株 * 2.5
      assertResult(BigDecimal("114000"))(
        BigDecimal(asset2Somy.evaluationAmount)
      ) // floor2(190000 * 0.60 / 3.0) = 38000株 * 3.0
      assertResult(BigDecimal("10000"))(BigDecimal(asset2.cashAmount)) // 200000 - 76000 - 114000

      When("最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする")
      pc.updateOptimalPortfolio(
        UpdateOptimalPortfolioRequest(
          Seq(PortfolioItemDto("Toyopa", "0.10"), PortfolioItemDto("Somy", "0.90"))
        )
      ).await
      oc.rebalanceOrder(RebalanceOrderRequest(userId)).await

      Then("リバランス後も資産合計がほぼ変わらない")
      val asset3 = ac.getAsset(GetAssetRequest(userId)).await
      val total3 = BigDecimal(asset3.cashAmount) + asset3.stocks.map(e => BigDecimal(e.evaluationAmount)).sum
      assertResult(true)((total3 - total2).abs <= BigDecimal(4))

      And("現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 19,000 円(10%)、Somy の評価額が 171,000 円(90%) となる")
      // total = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
      val asset3Toyopa = asset3.stocks.find(_.symbol == "Toyopa").get
      val asset3Somy = asset3.stocks.find(_.symbol == "Somy").get
      assertResult(BigDecimal("19000"))(
        BigDecimal(asset3Toyopa.evaluationAmount)
      ) // floor2(190000 * 0.10 / 2.5) = 7600株 * 2.5
      assertResult(BigDecimal("171000"))(
        BigDecimal(asset3Somy.evaluationAmount)
      ) // floor2(190000 * 0.90 / 3.0) = 57000株 * 3.0
      assertResult(BigDecimal("10000"))(BigDecimal(asset3.cashAmount)) // 200000 - 19000 - 171000
    }
  }
}
