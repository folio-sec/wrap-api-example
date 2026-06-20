import Decimal from "decimal.js";
import { UserId } from "../domain/userId";
import { BadRequestException } from "./presentationException";

export function parseUserId(s: string): UserId {
  try {
    return new UserId(s);
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    throw new BadRequestException(msg);
  }
}

export function parseAmount(s: string): Decimal {
  try {
    return new Decimal(s);
  } catch {
    throw new BadRequestException(`invalid amount: ${s}`);
  }
}
