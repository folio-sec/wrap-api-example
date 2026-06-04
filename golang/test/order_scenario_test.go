package test

import (
	"fmt"
	"testing"

	"folio/codinginterview/internal/infrastructure/server"
	"folio/codinginterview/internal/presentation"

	"github.com/shopspring/decimal"
)

func TestOrderScenario(t *testing.T) {
	s := server.NewDefaultDummyServer()
	ac := s.AssetController
	pc := s.PortfolioController
	oc := s.OrderController
	mp := s.MarketPriceController

	// initialize market price and optimal portfolio
	err := pc.UpdateOptimalPortfolio(presentation.UpdateOptimalPortfolioRequest{
		Portfolios: []presentation.PortfolioItemDto{
			{Symbol: "Toyopa", Rate: "0.40"},
			{Symbol: "Somy", Rate: "0.60"},
		},
	})
	if err != nil {
		t.Fatalf("setup UpdateOptimalPortfolio failed: %v", err)
	}
	err = mp.UpdateMarketPrice(presentation.UpdateMarketPriceRequest{
		MarketPrices: []presentation.MarketPriceItemDto{
			{Symbol: "Toyopa", MarketPrice: "2.5"},
			{Symbol: "Somy", MarketPrice: "3.0"},
		},
	})
	if err != nil {
		t.Fatalf("setup UpdateMarketPrice failed: %v", err)
	}

	userId := fmt.Sprintf("test-user-%d", 1)

	t.Log("存在しないユーザーで資産を取得しようとする")
	_, err = ac.GetAsset(presentation.GetAssetRequest{UserId: userId})
	if _, ok := presentation.IsBadRequestError(err); !ok {
		t.Fatalf("GetAsset should return BadRequestError for unknown user, got: %v", err)
	}
	t.Log("BadRequestException が返される")

	t.Log("最適ポートフォリオを Toyopa=40%, Somy=60% に更新する")
	err = pc.UpdateOptimalPortfolio(presentation.UpdateOptimalPortfolioRequest{
		Portfolios: []presentation.PortfolioItemDto{
			{Symbol: "Toyopa", Rate: "0.40"},
			{Symbol: "Somy", Rate: "0.60"},
		},
	})
	if err != nil {
		t.Fatalf("UpdateOptimalPortfolio failed: %v", err)
	}

	t.Log("新規拠出を 100,000 円で注文する")
	err = oc.NewContributionOrder(presentation.NewContributionOrderRequest{UserId: userId, Amount: "100000"})
	if err != nil {
		t.Fatalf("NewContributionOrder failed: %v", err)
	}

	t.Log("現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の評価額が 38,000 円(40%)、Somy の評価額が 57,000 円(60%) となる")
	// investable = 100000 - floor0(100000 * 0.05) = 95000
	asset1, err := ac.GetAsset(presentation.GetAssetRequest{UserId: userId})
	if err != nil {
		t.Fatalf("GetAsset failed: %v", err)
	}
	symbols1 := make(map[string]struct{})
	for _, s := range asset1.Stocks {
		symbols1[s.Symbol] = struct{}{}
	}
	if _, ok := symbols1["Toyopa"]; !ok {
		t.Error("asset1 should contain Toyopa")
	}
	if _, ok := symbols1["Somy"]; !ok {
		t.Error("asset1 should contain Somy")
	}
	total1 := mustDecimal(asset1.CashAmount)
	for _, s := range asset1.Stocks {
		total1 = total1.Add(mustDecimal(s.EvaluationAmount))
	}
	diff1 := total1.Sub(decimal.NewFromInt(100000)).Abs()
	if diff1.GreaterThan(decimal.NewFromInt(2)) {
		t.Errorf("asset1 total should be ~100000, got %s", total1.String())
	}

	asset1Toyopa := findStock(asset1.Stocks, "Toyopa")
	asset1Somy := findStock(asset1.Stocks, "Somy")
	assertDecimalEqual(t, "asset1 Toyopa evaluation", asset1Toyopa.EvaluationAmount, "38000") // floor2(95000 * 0.40 / 2.5) = 15200株 * 2.5
	assertDecimalEqual(t, "asset1 Somy evaluation", asset1Somy.EvaluationAmount, "57000")    // floor2(95000 * 0.60 / 3.0) = 19000株 * 3.0
	assertDecimalEqual(t, "asset1 cash", asset1.CashAmount, "5000")                          // 100000 - 38000 - 57000

	t.Log("追加拠出を 100,000 円で注文する")
	err = oc.AdditionalContributionOrder(presentation.AdditionalContributionOrderRequest{UserId: userId, Amount: "100000"})
	if err != nil {
		t.Fatalf("AdditionalContributionOrder failed: %v", err)
	}

	t.Log("資産合計が約 200,000 円になる")
	asset2, err := ac.GetAsset(presentation.GetAssetRequest{UserId: userId})
	if err != nil {
		t.Fatalf("GetAsset failed: %v", err)
	}
	total2 := mustDecimal(asset2.CashAmount)
	for _, s := range asset2.Stocks {
		total2 = total2.Add(mustDecimal(s.EvaluationAmount))
	}
	diff2 := total2.Sub(decimal.NewFromInt(200000)).Abs()
	if diff2.GreaterThan(decimal.NewFromInt(4)) {
		t.Errorf("asset2 total should be ~200000, got %s", total2.String())
	}

	t.Log("現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 76,000 円(40%)、Somy の評価額が 114,000 円(60%) となる")
	asset2Toyopa := findStock(asset2.Stocks, "Toyopa")
	asset2Somy := findStock(asset2.Stocks, "Somy")
	assertDecimalEqual(t, "asset2 Toyopa evaluation", asset2Toyopa.EvaluationAmount, "76000")  // floor2(190000 * 0.40 / 2.5) = 30400株 * 2.5
	assertDecimalEqual(t, "asset2 Somy evaluation", asset2Somy.EvaluationAmount, "114000")    // floor2(190000 * 0.60 / 3.0) = 38000株 * 3.0
	assertDecimalEqual(t, "asset2 cash", asset2.CashAmount, "10000")                          // 200000 - 76000 - 114000

	t.Log("最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする")
	err = pc.UpdateOptimalPortfolio(presentation.UpdateOptimalPortfolioRequest{
		Portfolios: []presentation.PortfolioItemDto{
			{Symbol: "Toyopa", Rate: "0.10"},
			{Symbol: "Somy", Rate: "0.90"},
		},
	})
	if err != nil {
		t.Fatalf("UpdateOptimalPortfolio failed: %v", err)
	}
	err = oc.RebalanceOrder(presentation.RebalanceOrderRequest{UserId: userId})
	if err != nil {
		t.Fatalf("RebalanceOrder failed: %v", err)
	}

	t.Log("リバランス後も資産合計がほぼ変わらない")
	asset3, err := ac.GetAsset(presentation.GetAssetRequest{UserId: userId})
	if err != nil {
		t.Fatalf("GetAsset failed: %v", err)
	}
	total3 := mustDecimal(asset3.CashAmount)
	for _, s := range asset3.Stocks {
		total3 = total3.Add(mustDecimal(s.EvaluationAmount))
	}
	diff3 := total3.Sub(total2).Abs()
	if diff3.GreaterThan(decimal.NewFromInt(4)) {
		t.Errorf("asset3 total should be ~%s (total2), got %s", total2.String(), total3.String())
	}

	t.Log("現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 19,000 円(10%)、Somy の評価額が 171,000 円(90%) となる")
	asset3Toyopa := findStock(asset3.Stocks, "Toyopa")
	asset3Somy := findStock(asset3.Stocks, "Somy")
	assertDecimalEqual(t, "asset3 Toyopa evaluation", asset3Toyopa.EvaluationAmount, "19000")  // floor2(190000 * 0.10 / 2.5) = 7600株 * 2.5
	assertDecimalEqual(t, "asset3 Somy evaluation", asset3Somy.EvaluationAmount, "171000")    // floor2(190000 * 0.90 / 3.0) = 57000株 * 3.0
	assertDecimalEqual(t, "asset3 cash", asset3.CashAmount, "10000")                          // 200000 - 19000 - 171000
}

func findStock(stocks []presentation.StockDto, symbol string) presentation.StockDto {
	for _, s := range stocks {
		if s.Symbol == symbol {
			return s
		}
	}
	return presentation.StockDto{}
}

func mustDecimal(s string) decimal.Decimal {
	d, err := decimal.NewFromString(s)
	if err != nil {
		panic(fmt.Sprintf("invalid decimal %q: %v", s, err))
	}
	return d
}
