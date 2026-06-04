import Decimal from "decimal.js";
import { randomUUID } from "node:crypto";
import { beforeEach, describe, expect, it } from "vitest";
import { DummyServer } from "../src/infrastructure/server/dummyServer.js";
import { BadRequestException } from "../src/presentation/presentationException.js";

describe("Investment Operation", () => {
  let server: DummyServer;

  beforeEach(async () => {
    server = DummyServer.default();
    await server.portfolioController.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.40" },
        { symbol: "Somy", rate: "0.60" },
      ],
    });
    await server.marketPriceController.updateMarketPrice({
      market_prices: [
        { symbol: "Toyopa", market_price: "2.5" },
        { symbol: "Somy", market_price: "3.0" },
      ],
    });
  });

  it("新規拠出・追加拠出・リバランスの一連の操作が正しく機能する", async () => {
    const ac = server.assetController;
    const pc = server.portfolioController;
    const oc = server.orderController;

    const userId = randomUUID();

    // Given: 存在しないユーザーで資産を取得しようとする
    let notFound: unknown;
    try {
      await ac.getAsset({ userId });
    } catch (e) {
      notFound = e;
    }
    // Then: BadRequestException が返される
    expect(notFound instanceof BadRequestException).toBe(true);

    // When: 最適ポートフォリオを Toyopa=40%, Somy=60% に更新する
    await pc.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.40" },
        { symbol: "Somy", rate: "0.60" },
      ],
    });

    // And: 新規拠出を 100,000 円で注文する
    await oc.newContributionOrder({ userId, amount: "100000" });

    const asset1 = await ac.getAsset({ userId });
    expect(new Set(asset1.stocks.map((e) => e.symbol))).toEqual(new Set(["Toyopa", "Somy"]));
    const total1 = asset1.stocks
      .map((e) => new Decimal(e.evaluationAmount))
      .reduce((acc, v) => acc.plus(v), new Decimal(0))
      .plus(new Decimal(asset1.cashAmount));
    expect(total1.minus(100000).abs().lessThanOrEqualTo(2)).toBe(true);

    // Then: 現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の評価額が 38,000 円(40%)、Somy の評価額が 57,000 円(60%) となる
    // investable = 100000 - floor0(100000 * 0.05) = 95000
    const asset1Toyopa = asset1.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset1Somy = asset1.stocks.find((e) => e.symbol === "Somy")!;
    expect(new Decimal(asset1Toyopa.evaluationAmount).equals("38000")).toBe(true); // floor2(95000 * 0.40 / 2.5) = 15200株 * 2.5
    expect(new Decimal(asset1Somy.evaluationAmount).equals("57000")).toBe(true); // floor2(95000 * 0.60 / 3.0) = 19000株 * 3.0
    expect(new Decimal(asset1.cashAmount).equals("5000")).toBe(true); // 100000 - 38000 - 57000

    // When: 追加拠出を 100,000 円で注文する
    await oc.additionalContributionOrder({ userId, amount: "100000" });

    // Then: 資産合計が約 200000 円になる
    const asset2 = await ac.getAsset({ userId });
    const total2 = asset2.stocks
      .map((e) => new Decimal(e.evaluationAmount))
      .reduce((acc, v) => acc.plus(v), new Decimal(0))
      .plus(new Decimal(asset2.cashAmount));
    expect(total2.minus(200000).abs().lessThanOrEqualTo(4)).toBe(true);

    // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 76,000 円(40%)、Somy の評価額が 114,000 円(60%) となる
    // totalAfter = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
    const asset2Toyopa = asset2.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset2Somy = asset2.stocks.find((e) => e.symbol === "Somy")!;
    expect(new Decimal(asset2Toyopa.evaluationAmount).equals("76000")).toBe(true); // floor2(190000 * 0.40 / 2.5) = 30400株 * 2.5
    expect(new Decimal(asset2Somy.evaluationAmount).equals("114000")).toBe(true); // floor2(190000 * 0.60 / 3.0) = 38000株 * 3.0
    expect(new Decimal(asset2.cashAmount).equals("10000")).toBe(true); // 200000 - 76000 - 114000

    // When: 最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする
    await pc.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.10" },
        { symbol: "Somy", rate: "0.90" },
      ],
    });
    await oc.rebalanceOrder({ userId });

    // Then: リバランス後も資産合計がほぼ変わらない
    const asset3 = await ac.getAsset({ userId });
    const total3 = asset3.stocks
      .map((e) => new Decimal(e.evaluationAmount))
      .reduce((acc, v) => acc.plus(v), new Decimal(0))
      .plus(new Decimal(asset3.cashAmount));
    expect(total3.minus(total2).abs().lessThanOrEqualTo(4)).toBe(true);

    // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 19,000 円(10%)、Somy の評価額が 171,000 円(90%) となる
    // total = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
    const asset3Toyopa = asset3.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset3Somy = asset3.stocks.find((e) => e.symbol === "Somy")!;
    expect(new Decimal(asset3Toyopa.evaluationAmount).equals("19000")).toBe(true); // floor2(190000 * 0.10 / 2.5) = 7600株 * 2.5
    expect(new Decimal(asset3Somy.evaluationAmount).equals("171000")).toBe(true); // floor2(190000 * 0.90 / 3.0) = 57000株 * 3.0
    expect(new Decimal(asset3.cashAmount).equals("10000")).toBe(true); // 200000 - 19000 - 171000
  });
});
