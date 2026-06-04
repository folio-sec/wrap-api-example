package test

import (
	"testing"

	"folio/codinginterview/internal/infrastructure/server"
	"folio/codinginterview/internal/presentation"

	"github.com/shopspring/decimal"
)

func TestOptimalPortfolioScenario(t *testing.T) {
	s := server.NewDefaultDummyServer()
	pc := s.PortfolioController

	t.Log("最適ポートフォリオを Toyopa=0.20, Somy=0.80 に更新する")
	err := pc.UpdateOptimalPortfolio(presentation.UpdateOptimalPortfolioRequest{
		Portfolios: []presentation.PortfolioItemDto{
			{Symbol: "Toyopa", Rate: "0.20"},
			{Symbol: "Somy", Rate: "0.80"},
		},
	})
	if err != nil {
		t.Fatalf("UpdateOptimalPortfolio failed: %v", err)
	}

	t.Log("最適ポートフォリオを取得する")
	first, err := pc.GetOptimalPortfolio()
	if err != nil {
		t.Fatalf("GetOptimalPortfolio failed: %v", err)
	}
	firstMap := make(map[string]string)
	for _, p := range first.Portfolios {
		firstMap[p.Symbol] = p.Rate
	}

	t.Log("Toyopa=0.20, Somy=0.80 が返される")
	assertDecimalEqual(t, "Toyopa rate", firstMap["Toyopa"], "0.20")
	assertDecimalEqual(t, "Somy rate", firstMap["Somy"], "0.80")

	t.Log("最適ポートフォリオを Toyopa=0.40, Somy=0.60 に更新して再取得する")
	err = pc.UpdateOptimalPortfolio(presentation.UpdateOptimalPortfolioRequest{
		Portfolios: []presentation.PortfolioItemDto{
			{Symbol: "Toyopa", Rate: "0.40"},
			{Symbol: "Somy", Rate: "0.60"},
		},
	})
	if err != nil {
		t.Fatalf("UpdateOptimalPortfolio failed: %v", err)
	}
	second, err := pc.GetOptimalPortfolio()
	if err != nil {
		t.Fatalf("GetOptimalPortfolio failed: %v", err)
	}
	secondMap := make(map[string]string)
	for _, p := range second.Portfolios {
		secondMap[p.Symbol] = p.Rate
	}

	t.Log("Toyopa=0.40, Somy=0.60 が返される")
	assertDecimalEqual(t, "Toyopa rate", secondMap["Toyopa"], "0.40")
	assertDecimalEqual(t, "Somy rate", secondMap["Somy"], "0.60")
}

func assertDecimalEqual(t *testing.T, label, got, want string) {
	t.Helper()
	g, err := decimal.NewFromString(got)
	if err != nil {
		t.Fatalf("%s: invalid got value %q: %v", label, got, err)
	}
	w, err := decimal.NewFromString(want)
	if err != nil {
		t.Fatalf("%s: invalid want value %q: %v", label, want, err)
	}
	if !g.Equal(w) {
		t.Errorf("%s: got %s, want %s", label, got, want)
	}
}
