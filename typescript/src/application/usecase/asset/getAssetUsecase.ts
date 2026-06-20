import Decimal from "decimal.js";
import { AccountRepository } from "../../repository/accountRepository";
import { StockSymbol } from "../../../domain/stockSymbol";
import { UserId } from "../../../domain/userId";

export interface GetAssetUsecaseInput {
  userId: UserId;
}

export interface GetAssetStockOutput {
  symbol: StockSymbol;
  amountJpy: Decimal;
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
  constructor(private readonly accountRepository: AccountRepository) {}

  async run(input: GetAssetUsecaseInput): Promise<GetAssetUsecaseOutput> {
    const account = await this.accountRepository.find(input.userId);
    if (account === undefined) {
      throw new UserNotFoundException();
    }
    const stocks = account.stocks.map((e) => ({
      symbol: e.symbol,
      amountJpy: e.amountJpy,
    }));
    return { cashAmount: account.cash, stocks };
  }
}
