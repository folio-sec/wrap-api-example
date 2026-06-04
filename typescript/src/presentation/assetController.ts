import {
  GetAssetUsecase,
  UserNotFoundException,
} from "../application/usecase/asset/getAssetUsecase.js";
import { BadRequestException } from "./presentationException.js";
import { parseUserId } from "./presentationPreparation.js";

export interface StockDto {
  symbol: string;
  evaluationAmount: string;
}

export interface GetAssetRequest {
  userId: string;
}

export interface GetAssetResponse {
  cashAmount: string;
  stocks: StockDto[];
}

export class AssetController {
  constructor(private readonly getAssetUsecase: GetAssetUsecase) {}

  async getAsset(req: GetAssetRequest): Promise<GetAssetResponse> {
    const uid = parseUserId(req.userId);
    try {
      const out = await this.getAssetUsecase.run({ userId: uid });
      return {
        cashAmount: out.cashAmount.toString(),
        stocks: out.stocks.map((e) => ({
          symbol: e.symbol,
          evaluationAmount: e.evaluationAmount.toString(),
        })),
      };
    } catch (e) {
      if (e instanceof UserNotFoundException) {
        throw new BadRequestException("user not found");
      }
      throw e;
    }
  }
}
