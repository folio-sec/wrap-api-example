from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class UserId:
    """ユーザーIDを表す。"""

    value: str

    def __post_init__(self) -> None:
        if not self.value:
            raise ValueError("userId must not be empty")
