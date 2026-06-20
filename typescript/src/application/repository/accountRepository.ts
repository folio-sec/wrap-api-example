import { Account } from "../../domain/account";
import { UserId } from "../../domain/userId";

/** 口座管理リポジトリ。 */
export interface AccountRepository {
  find(userId: UserId): Promise<Account | undefined>;
  upsert(userId: UserId, account: Account): Promise<void>;
  exists(userId: UserId): Promise<boolean>;
}
