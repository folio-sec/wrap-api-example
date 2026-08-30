import Decimal from "decimal.js";
import { randomUUID } from "node:crypto";
import { beforeEach, describe, expect, it } from "vitest";
import { DummyServer } from "../src/infrastructure/server/dummyServer.js";
import { BadRequestException } from "../src/presentation/presentationException.js";

describe("Investment Operation", () => {
  let server: DummyServer;

  // 失敗時に実測値がメッセージへ出るようにする(比較は Decimal 同士のまま)
  const expectAmount = (label: string, actual: string, expected: string) => {
    expect(
      new Decimal(actual).equals(expected),
      `${label}: got ${actual}, want ${expected}`,
    ).toBe(true);
  };

  const totalOf = (asset: { cashAmount: string; stocks: { amountJpy: string }[] }) =>
    asset.stocks
      .map((e) => new Decimal(e.amountJpy))
      .reduce((acc, v) => acc.plus(v), new Decimal(asset.cashAmount));

  beforeEach(async () => {
    server = DummyServer.default();
    await server.portfolioController.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.40" },
        { symbol: "Somy", rate: "0.60" },
      ],
    });
  });

  it("新規注文・追加注文・リバランスの一連の操作が正しく機能する", async () => {
    const userId = randomUUID();

    // Given: 存在しないユーザーで資産を取得しようとする
    let notFound: unknown;
    try {
      await server.assetController.getAsset({ userId });
    } catch (e) {
      notFound = e;
    }
    // Then: BadRequestException が返される
    expect(notFound instanceof BadRequestException).toBe(true);

    // When: 最適ポートフォリオを Toyopa=40%, Somy=60% に更新する
    await server.portfolioController.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.40" },
        { symbol: "Somy", rate: "0.60" },
      ],
    });

    // And: 新規注文を 100,000 円で注文する
    await server.orderController.newOrder({ userId, amount: "100000" });

    const asset1 = await server.assetController.getAsset({ userId });
    expect(new Set(asset1.stocks.map((e) => e.symbol))).toEqual(new Set(["Toyopa", "Somy"]));
    const total1 = totalOf(asset1);
    expect(total1.minus(100000).abs().lessThanOrEqualTo(2)).toBe(true);

    // Then: 現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の保有額が 38,000 円(40%)、Somy の保有額が 57,000 円(60%) となる
    // cash = floor0(100000 * 0.05) = 5000, investable = 100000 - 5000 = 95000
    const asset1Toyopa = asset1.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset1Somy = asset1.stocks.find((e) => e.symbol === "Somy")!;
    expectAmount("asset1 Toyopa amount", asset1Toyopa.amountJpy, "38000"); // floor0(95000 * 0.40) = 38000
    expectAmount("asset1 Somy amount", asset1Somy.amountJpy, "57000");     // floor0(95000 * 0.60) = 57000
    expectAmount("asset1 cash", asset1.cashAmount, "5000");                // 100000 - 38000 - 57000

    // When: 追加注文を 100,000 円で注文する
    await server.orderController.additionalOrder({ userId, amount: "100000" });

    // Then: 資産合計が約 200,000 円になる
    const asset2 = await server.assetController.getAsset({ userId });
    const total2 = totalOf(asset2);
    expect(total2.minus(200000).abs().lessThanOrEqualTo(4)).toBe(true);

    // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の保有額が 76,000 円(40%)、Somy の保有額が 114,000 円(60%) となる
    // totalAfter = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
    const asset2Toyopa = asset2.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset2Somy = asset2.stocks.find((e) => e.symbol === "Somy")!;
    expectAmount("asset2 Toyopa amount", asset2Toyopa.amountJpy, "76000");  // floor0(190000 * 0.40) = 76000
    expectAmount("asset2 Somy amount", asset2Somy.amountJpy, "114000");     // floor0(190000 * 0.60) = 114000
    expectAmount("asset2 cash", asset2.cashAmount, "10000");                // 200000 - 76000 - 114000

    // When: 最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする
    await server.portfolioController.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.10" },
        { symbol: "Somy", rate: "0.90" },
      ],
    });
    await server.orderController.rebalanceOrder({ userId });

    // Then: リバランス後も資産合計がほぼ変わらない
    const asset3 = await server.assetController.getAsset({ userId });
    const total3 = totalOf(asset3);
    expect(total3.minus(total2).abs().lessThanOrEqualTo(4)).toBe(true);

    // And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の保有額が 19,000 円(10%)、Somy の保有額が 171,000 円(90%) となる
    // total = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
    const asset3Toyopa = asset3.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset3Somy = asset3.stocks.find((e) => e.symbol === "Somy")!;
    expectAmount("asset3 Toyopa amount", asset3Toyopa.amountJpy, "19000");  // floor0(190000 * 0.10) = 19000
    expectAmount("asset3 Somy amount", asset3Somy.amountJpy, "171000");     // floor0(190000 * 0.90) = 171000
    expectAmount("asset3 cash", asset3.cashAmount, "10000");                // 200000 - 19000 - 171000
  });
});
