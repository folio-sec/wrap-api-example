import {
  AdditionalBuyAmountTooSmallException,
  AdditionalBuyOrderUsecase,
  AdditionalBuyUserNotFoundException,
} from "../application/usecase/order/additionalBuyOrderUsecase.js";
import {
  NewOrderAmountTooSmallException,
  NewOrderUsecase,
  NewOrderUserAlreadyExistsException,
} from "../application/usecase/order/newOrderUsecase.js";
import {
  RebalanceOrderUsecase,
  RebalanceUserNotFoundException,
} from "../application/usecase/order/rebalanceOrderUsecase.js";
import { BadRequestException } from "./presentationException.js";
import { parseAmount, parseUserId } from "./presentationPreparation.js";

export interface NewOrderRequest {
  userId: string;
  amount: string;
}

export interface AdditionalOrderRequest {
  userId: string;
  amount: string;
}

export interface RebalanceOrderRequest {
  userId: string;
}

export class OrderController {
  constructor(
    private readonly newOrderUsecase: NewOrderUsecase,
    private readonly additionalBuyOrderUsecase: AdditionalBuyOrderUsecase,
    private readonly rebalanceOrderUsecase: RebalanceOrderUsecase,
  ) {}

  async newOrder(req: NewOrderRequest): Promise<void> {
    const uid = parseUserId(req.userId);
    const amt = parseAmount(req.amount);
    try {
      await this.newOrderUsecase.run({ userId: uid, amount: amt });
    } catch (e) {
      if (e instanceof NewOrderUserAlreadyExistsException) {
        throw new BadRequestException("user already has account");
      }
      if (e instanceof NewOrderAmountTooSmallException) {
        throw new BadRequestException("amount is too small");
      }
      throw e;
    }
  }

  async additionalOrder(req: AdditionalOrderRequest): Promise<void> {
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
