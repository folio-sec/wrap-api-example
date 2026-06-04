import Decimal from "decimal.js";
import { AccountRepository } from "../../repository/accountRepository.js";
import { MarketPriceRepository } from "../../repository/marketPriceRepository.js";
import { AssetService } from "../../service/assetService.js";
import { StockSymbol } from "../../../domain/stockSymbol.js";
import { UserId } from "../../../domain/userId.js";

export interface GetAssetUsecaseInput {
  userId: UserId;
}

export interface GetAssetStockOutput {
  symbol: StockSymbol;
  evaluationAmount: Decimal;
}

export interface GetAssetUsecaseOutput {
  cashAmount: Decimal;
  stocks: GetAssetStockOutput[];
}

export class GetAssetUsecaseException extends Error {}
export class UserNotFoundException extends GetAssetUsecaseException {
  constructor() {
    super("user not found");
  }
}

export class GetAssetUsecase {
  constructor(
    private readonly accountRepository: AccountRepository,
    private readonly marketPriceRepository: MarketPriceRepository,
  ) {}

  async run(input: GetAssetUsecaseInput): Promise<GetAssetUsecaseOutput> {
    const account = await this.accountRepository.find(input.userId);
    if (account === undefined) {
      throw new UserNotFoundException();
    }
    const prices = await this.marketPriceRepository.all();
    const stocks = account.stocks.map((e) => ({
      symbol: e.symbol,
      evaluationAmount: AssetService.evaluateStock(e, prices),
    }));
    return { cashAmount: account.cash, stocks };
  }
}
