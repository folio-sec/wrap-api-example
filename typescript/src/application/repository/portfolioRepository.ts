import { Portfolio } from "../../domain/stock.js";

/** 最適ポートフォリオリポジトリ。 */
export interface PortfolioRepository {
  get(): Promise<Portfolio>;
  update(portfolio: Portfolio): Promise<void>;
}
