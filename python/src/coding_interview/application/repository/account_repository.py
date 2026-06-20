from __future__ import annotations

from typing import Protocol

from coding_interview.domain.account import Account
from coding_interview.domain.user_id import UserId


class AccountRepository(Protocol):
    """口座管理リポジトリ。"""

    def find(self, user_id: UserId) -> Account | None: ...
    def upsert(self, user_id: UserId, account: Account) -> None: ...
    def exists(self, user_id: UserId) -> bool: ...
