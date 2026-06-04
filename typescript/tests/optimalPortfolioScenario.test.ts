import Decimal from "decimal.js";
import { describe, it, expect } from "vitest";
import { DummyServer } from "../src/infrastructure/server/dummyServer.js";

describe("Optimal Portfolio Management", () => {
  it("最適ポートフォリオを更新・取得できる", async () => {
    const server = DummyServer.default();
    const pc = server.portfolioController;

    // Given: 最適ポートフォリオを Toyopa=0.20, Somy=0.80 に更新する
    await pc.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.20" },
        { symbol: "Somy", rate: "0.80" },
      ],
    });

    // When: 最適ポートフォリオを取得する
    const first = await pc.getOptimalPortfolio();
    const firstMap = new Map(first.portfolios.map((p) => [p.symbol, p.rate]));

    // Then: Toyopa=0.20, Somy=0.80 が返される
    expect(new Decimal(firstMap.get("Toyopa")!).equals(new Decimal("0.20"))).toBe(true);
    expect(new Decimal(firstMap.get("Somy")!).equals(new Decimal("0.80"))).toBe(true);

    // When: 最適ポートフォリオを Toyopa=0.40, Somy=0.60 に更新して再取得する
    await pc.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.40" },
        { symbol: "Somy", rate: "0.60" },
      ],
    });
    const second = await pc.getOptimalPortfolio();
    const secondMap = new Map(second.portfolios.map((p) => [p.symbol, p.rate]));

    // Then: Toyopa=0.40, Somy=0.60 が返される
    expect(new Decimal(secondMap.get("Toyopa")!).equals(new Decimal("0.40"))).toBe(true);
    expect(new Decimal(secondMap.get("Somy")!).equals(new Decimal("0.60"))).toBe(true);
  });
});
