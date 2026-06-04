import {
  AdditionalBuyAmountTooSmallException,
  AdditionalBuyOrderUsecase,
  AdditionalBuyUserNotFoundException,
} from "../application/usecase/order/additionalBuyOrderUsecase.js";
import {
  NewContributionAmountTooSmallException,
  NewContributionOrderUsecase,
  NewContributionUserAlreadyExistsException,
} from "../application/usecase/order/newContributionOrderUsecase.js";
import {
  RebalanceOrderUsecase,
  RebalanceUserNotFoundException,
} from "../application/usecase/order/rebalanceOrderUsecase.js";
import { BadRequestException } from "./presentationException.js";
import { parseAmount, parseUserId } from "./presentationPreparation.js";

export interface NewContributionOrderRequest {
  userId: string;
  amount: string;
}

export interface AdditionalContributionOrderRequest {
  userId: string;
  amount: string;
}

export interface RebalanceOrderRequest {
  userId: string;
}

export class OrderController {
  constructor(
    private readonly newContributionOrderUsecase: NewContributionOrderUsecase,
    private readonly additionalBuyOrderUsecase: AdditionalBuyOrderUsecase,
    private readonly rebalanceOrderUsecase: RebalanceOrderUsecase,
  ) {}

  async newContributionOrder(req: NewContributionOrderRequest): Promise<void> {
    const uid = parseUserId(req.userId);
    const amt = parseAmount(req.amount);
    try {
      await this.newContributionOrderUsecase.run({ userId: uid, amount: amt });
    } catch (e) {
      if (e instanceof NewContributionUserAlreadyExistsException) {
        throw new BadRequestException("user already has account");
      }
      if (e instanceof NewContributionAmountTooSmallException) {
        throw new BadRequestException("amount is too small");
      }
      throw e;
    }
  }

  async additionalContributionOrder(req: AdditionalContributionOrderRequest): Promise<void> {
    const uid = parseUserId(req.userId);
    const amt = parseAmount(req.amount);
    try {
      await this.additionalBuyOrderUsecase.run({ userId: uid, amount: amt });
    } catch (e) {
      if (e instanceof AdditionalBuyUserNotFoundException) {
        throw new BadRequestException("user has no live account");
      }
      if (e instanceof AdditionalBuyAmountTooSmallException) {
        throw new BadRequestException("amount is too small");
      }
      throw e;
    }
  }

  async rebalanceOrder(req: RebalanceOrderRequest): Promise<void> {
    const uid = parseUserId(req.userId);
    try {
      await this.rebalanceOrderUsecase.run({ userId: uid });
    } catch (e) {
      if (e instanceof RebalanceUserNotFoundException) {
        throw new BadRequestException("user has no live account");
      }
      throw e;
    }
  }
}
