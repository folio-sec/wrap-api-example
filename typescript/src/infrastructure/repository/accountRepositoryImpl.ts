import { AccountRepository } from "../../application/repository/accountRepository";
import { Account } from "../../domain/account";
import { UserId } from "../../domain/userId";

export class AccountRepositoryImpl implements AccountRepository {
  private readonly store: Map<string, Account> = new Map();

  async find(userId: UserId): Promise<Account | undefined> {
    return this.store.get(userId.value);
  }

  async upsert(userId: UserId, account: Account): Promise<void> {
    this.store.set(userId.value, account);
  }

  async exists(userId: UserId): Promise<boolean> {
    return this.store.has(userId.value);
  }
}
