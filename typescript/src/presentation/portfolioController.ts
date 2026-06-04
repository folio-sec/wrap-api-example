import Decimal from "decimal.js";
import {
  GetLatestPortfolioUsecase,
} from "../application/usecase/portfolio/getLatestPortfolioUsecase.js";
import {
  InvalidPortfolioException,
  UpdatePortfolioItemInput,
  UpdatePortfolioUsecase,
} from "../application/usecase/portfolio/updatePortfolioUsecase.js";
import { StockSymbol } from "../domain/stockSymbol.js";
import { BadRequestException } from "./presentationException.js";

export interface PortfolioItemDto {
  symbol: string;
  rate: string;
}

export interface GetOptimalPortfolioResponse {
  portfolios: PortfolioItemDto[];
}

export interface UpdateOptimalPortfolioRequest {
  portfolios: PortfolioItemDto[];
}

export class PortfolioController {
  constructor(
    private readonly getLatestPortfolioUsecase: GetLatestPortfolioUsecase,
    private readonly updatePortfolioUsecase: UpdatePortfolioUsecase,
  ) {}

  async getOptimalPortfolio(): Promise<GetOptimalPortfolioResponse> {
    const out = await this.getLatestPortfolioUsecase.run();
    return {
      portfolios: out.items.map((i) => ({ symbol: i.symbol, rate: i.rate.toString() })),
    };
  }

  async updateOptimalPortfolio(req: UpdateOptimalPortfolioRequest): Promise<void> {
    const items: UpdatePortfolioItemInput[] = [];
    for (const dto of req.portfolios) {
      const sym = StockSymbol.fromString(dto.symbol);
      if (sym === undefined) {
        throw new BadRequestException(`unknown symbol: ${dto.symbol}`);
      }
      let rate: Decimal;
      try {
        rate = new Decimal(dto.rate);
      } catch {
        throw new BadRequestException(`invalid rate: ${dto.rate}`);
      }
      items.push({ symbol: sym, rate });
    }
    try {
      await this.updatePortfolioUsecase.run({ items });
    } catch (e) {
      if (e instanceof InvalidPortfolioException) {
        throw new BadRequestException(e.message);
      }
      throw e;
    }
  }
}
