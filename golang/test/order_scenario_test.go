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

	// initialize optimal portfolio
	err := pc.UpdateOptimalPortfolio(presentation.UpdateOptimalPortfolioRequest{
		Portfolios: []presentation.PortfolioItemDto{
			{Symbol: "Toyopa", Rate: "0.40"},
			{Symbol: "Somy", Rate: "0.60"},
		},
	})
	if err != nil {
		t.Fatalf("setup UpdateOptimalPortfolio failed: %v", err)
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

	t.Log("新規注文を 100,000 円で行う")
	err = oc.NewOrder(presentation.NewOrderRequest{UserId: userId, Amount: "100000"})
	if err != nil {
		t.Fatalf("NewOrder failed: %v", err)
	}

	t.Log("現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の保有額が 38,000 円(40%)、Somy の保有額が 57,000 円(60%) となる")
	// cash = floor0(100000 * 0.05) = 5000, investable = 100000 - 5000 = 95000
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
		total1 = total1.Add(mustDecimal(s.AmountJpy))
	}
	diff1 := total1.Sub(decimal.NewFromInt(100000)).Abs()
	if diff1.GreaterThan(decimal.NewFromInt(2)) {
		t.Errorf("asset1 total should be ~100000, got %s", total1.String())
	}

	asset1Toyopa := findStock(asset1.Stocks, "Toyopa")
	asset1Somy := findStock(asset1.Stocks, "Somy")
	assertDecimalEqual(t, "asset1 Toyopa amount", asset1Toyopa.AmountJpy, "38000") // floor0(95000 * 0.40) = 38000
	assertDecimalEqual(t, "asset1 Somy amount", asset1Somy.AmountJpy, "57000")     // floor0(95000 * 0.60) = 57000
	assertDecimalEqual(t, "asset1 cash", asset1.CashAmount, "5000")                // 100000 - 38000 - 57000

	t.Log("追加注文を 100,000 円で行う")
	err = oc.AdditionalOrder(presentation.AdditionalOrderRequest{UserId: userId, Amount: "100000"})
	if err != nil {
		t.Fatalf("AdditionalOrder failed: %v", err)
	}

	t.Log("資産合計が約 200,000 円になる")
	asset2, err := ac.GetAsset(presentation.GetAssetRequest{UserId: userId})
	if err != nil {
		t.Fatalf("GetAsset failed: %v", err)
	}
	total2 := mustDecimal(asset2.CashAmount)
	for _, s := range asset2.Stocks {
		total2 = total2.Add(mustDecimal(s.AmountJpy))
	}
	diff2 := total2.Sub(decimal.NewFromInt(200000)).Abs()
	if diff2.GreaterThan(decimal.NewFromInt(4)) {
		t.Errorf("asset2 total should be ~200000, got %s", total2.String())
	}

	t.Log("現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の保有額が 76,000 円(40%)、Somy の保有額が 114,000 円(60%) となる")
	asset2Toyopa := findStock(asset2.Stocks, "Toyopa")
	asset2Somy := findStock(asset2.Stocks, "Somy")
	// totalAfter = 100000 + 100000 = 200000, cash = floor0(200000 * 0.05) = 10000, investable = 190000
	assertDecimalEqual(t, "asset2 Toyopa amount", asset2Toyopa.AmountJpy, "76000") // floor0(190000 * 0.40) = 76000
	assertDecimalEqual(t, "asset2 Somy amount", asset2Somy.AmountJpy, "114000")    // floor0(190000 * 0.60) = 114000
	assertDecimalEqual(t, "asset2 cash", asset2.CashAmount, "10000")               // 200000 - 76000 - 114000

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
		total3 = total3.Add(mustDecimal(s.AmountJpy))
	}
	diff3 := total3.Sub(total2).Abs()
	if diff3.GreaterThan(decimal.NewFromInt(4)) {
		t.Errorf("asset3 total should be ~%s (total2), got %s", total2.String(), total3.String())
	}

	t.Log("現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の保有額が 19,000 円(10%)、Somy の保有額が 171,000 円(90%) となる")
	asset3Toyopa := findStock(asset3.Stocks, "Toyopa")
	asset3Somy := findStock(asset3.Stocks, "Somy")
	// total = 200000, cash = floor0(200000 * 0.05) = 10000, investable = 190000
	assertDecimalEqual(t, "asset3 Toyopa amount", asset3Toyopa.AmountJpy, "19000") // floor0(190000 * 0.10) = 19000
	assertDecimalEqual(t, "asset3 Somy amount", asset3Somy.AmountJpy, "171000")    // floor0(190000 * 0.90) = 171000
	assertDecimalEqual(t, "asset3 cash", asset3.CashAmount, "10000")               // 200000 - 19000 - 171000
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
