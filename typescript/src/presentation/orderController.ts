import {
  AdditionalBuyAmountTooSmallException,
  AdditionalBuyOrderUsecase,
  AdditionalBuyUserNotFoundException,
} from "../application/usecase/order/additionalBuyOrderUsecase";
import {
  NewOrderAmountTooSmallException,
  NewOrderUsecase,
  NewOrderUserAlreadyExistsException,
} from "../application/usecase/order/newOrderUsecase";
import { BadRequestException } from "./presentationException";
import { parseAmount, parseUserId } from "./presentationPreparation";

export interface NewOrderRequest {
  userId: string;
  amount: string;
}

export interface AdditionalOrderRequest {
  userId: string;
  amount: string;
}

export class OrderController {
  constructor(
    private readonly newOrderUsecase: NewOrderUsecase,
    private readonly additionalBuyOrderUsecase: AdditionalBuyOrderUsecase,
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
}
