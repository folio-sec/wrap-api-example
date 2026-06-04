package folio.codinginterview

import folio.codinginterview.infrastructure.server.DummyServer
import folio.codinginterview.presentation.PortfolioController.PortfolioItemDto
import folio.codinginterview.presentation.PortfolioController.UpdateOptimalPortfolioRequest
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.*

class OptimalPortfolioScenario extends AnyFeatureSpec with GivenWhenThen {
  given ExecutionContext = ExecutionContext.global

  extension [A](f: Future[A]) def await: A = Await.result(f, 5.seconds)

  val server = DummyServer.default()
  val pc = server.portfolioController

  Feature("Optimal Portfolio Management") {
    Scenario("最適ポートフォリオを更新・取得できる") {

      Given("最適ポートフォリオを Toyopa=0.20, Somy=0.80 に更新する")
      pc.updateOptimalPortfolio(
        UpdateOptimalPortfolioRequest(
          Seq(PortfolioItemDto("Toyopa", "0.20"), PortfolioItemDto("Somy", "0.80"))
        )
      ).await

      When("最適ポートフォリオを取得する")
      val first = pc.getOptimalPortfolio().await
      val firstMap = first.portfolios.map(p => p.symbol -> p.rate).toMap

      Then("Toyopa=0.20, Somy=0.80 が返される")
      assertResult(BigDecimal("0.20"))(BigDecimal(firstMap("Toyopa")))
      assertResult(BigDecimal("0.80"))(BigDecimal(firstMap("Somy")))

      When("最適ポートフォリオを Toyopa=0.40, Somy=0.60 に更新して再取得する")
      pc.updateOptimalPortfolio(
        UpdateOptimalPortfolioRequest(
          Seq(PortfolioItemDto("Toyopa", "0.40"), PortfolioItemDto("Somy", "0.60"))
        )
      ).await
      val second = pc.getOptimalPortfolio().await
      val secondMap = second.portfolios.map(p => p.symbol -> p.rate).toMap

      Then("Toyopa=0.40, Somy=0.60 が返される")
      assertResult(BigDecimal("0.40"))(BigDecimal(secondMap("Toyopa")))
      assertResult(BigDecimal("0.60"))(BigDecimal(secondMap("Somy")))
    }
  }
}
