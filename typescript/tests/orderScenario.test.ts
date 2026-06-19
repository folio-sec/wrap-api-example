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
  });

  it("存在しないユーザーへのリクエストは BadRequestException を返す", async () => {
    const userId = randomUUID();
    let notFound: unknown;
    try {
      await server.assetController.getAsset({ userId });
    } catch (e) {
      notFound = e;
    }
    expect(notFound instanceof BadRequestException).toBe(true);
  });

  it("asset1: 新規注文 100,000円 が正しく機能する", async () => {
    const userId = randomUUID();
    // cash = floor0(100000 * 0.05) = 5000, investable = 100000 - 5000 = 95000
    await server.orderController.newOrder({ userId, amount: "100000" });
    const asset1 = await server.assetController.getAsset({ userId });
    expect(new Set(asset1.stocks.map((e) => e.symbol))).toEqual(new Set(["Toyopa", "Somy"]));
    const total1 = asset1.stocks
      .map((e) => new Decimal(e.amountJpy))
      .reduce((acc, v) => acc.plus(v), new Decimal(0))
      .plus(new Decimal(asset1.cashAmount));
    expect(total1.minus(100000).abs().lessThanOrEqualTo(2)).toBe(true);
    const asset1Toyopa = asset1.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset1Somy = asset1.stocks.find((e) => e.symbol === "Somy")!;
    expect(new Decimal(asset1Toyopa.amountJpy).equals("38000")).toBe(true); // floor0(95000 * 0.40) = 38000
    expect(new Decimal(asset1Somy.amountJpy).equals("57000")).toBe(true);   // floor0(95000 * 0.60) = 57000
    expect(new Decimal(asset1.cashAmount).equals("5000")).toBe(true);       // 100000 - 38000 - 57000
  });

  it("asset2: 追加注文 100,000円 が正しく機能する", async () => {
    const userId = randomUUID();
    // totalAfter = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
    await server.orderController.newOrder({ userId, amount: "100000" });
    await server.orderController.additionalOrder({ userId, amount: "100000" });
    const asset2 = await server.assetController.getAsset({ userId });
    const total2 = asset2.stocks
      .map((e) => new Decimal(e.amountJpy))
      .reduce((acc, v) => acc.plus(v), new Decimal(0))
      .plus(new Decimal(asset2.cashAmount));
    expect(total2.minus(200000).abs().lessThanOrEqualTo(4)).toBe(true);
    const asset2Toyopa = asset2.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset2Somy = asset2.stocks.find((e) => e.symbol === "Somy")!;
    expect(new Decimal(asset2Toyopa.amountJpy).equals("76000")).toBe(true); // floor0(190000 * 0.40) = 76000
    expect(new Decimal(asset2Somy.amountJpy).equals("114000")).toBe(true);  // floor0(190000 * 0.60) = 114000
    expect(new Decimal(asset2.cashAmount).equals("10000")).toBe(true);      // 200000 - 76000 - 114000
  });

  it("asset3: リバランス注文（課題2: 現金比率を確保しないバグがある）", async () => {
    const userId = randomUUID();
    await server.orderController.newOrder({ userId, amount: "100000" });
    await server.orderController.additionalOrder({ userId, amount: "100000" });
    await server.portfolioController.updateOptimalPortfolio({
      portfolios: [
        { symbol: "Toyopa", rate: "0.10" },
        { symbol: "Somy", rate: "0.90" },
      ],
    });
    await server.orderController.rebalanceOrder({ userId });
    // total = 200000; investable = 200000 - floor0(200000 * 0.05) = 190000
    const asset3 = await server.assetController.getAsset({ userId });
    const asset3Toyopa = asset3.stocks.find((e) => e.symbol === "Toyopa")!;
    const asset3Somy = asset3.stocks.find((e) => e.symbol === "Somy")!;
    expect(new Decimal(asset3Toyopa.amountJpy).equals("19000")).toBe(true); // floor0(190000 * 0.10) = 19000
    expect(new Decimal(asset3Somy.amountJpy).equals("171000")).toBe(true);  // floor0(190000 * 0.90) = 171000
    expect(new Decimal(asset3.cashAmount).equals("10000")).toBe(true);      // 200000 - 19000 - 171000
  });
});
