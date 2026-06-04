from __future__ import annotations

from coding_interview.domain.stock import Account
from coding_interview.domain.user_id import UserId


class AccountRepositoryImpl:
    def __init__(self) -> None:
        self._store: dict[str, Account] = {}

    def find(self, user_id: UserId) -> Account | None:
        return self._store.get(user_id.value)

    def upsert(self, user_id: UserId, account: Account) -> None:
        self._store[user_id.value] = account

    def exists(self, user_id: UserId) -> bool:
        return user_id.value in self._store
