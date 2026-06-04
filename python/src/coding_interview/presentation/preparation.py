from __future__ import annotations

from decimal import Decimal, InvalidOperation

from coding_interview.domain.user_id import UserId
from coding_interview.presentation.exceptions import BadRequestException


def parse_user_id(s: str) -> UserId:
    try:
        return UserId(s)
    except (ValueError, Exception) as e:
        raise BadRequestException(str(e)) from e


def parse_amount(s: str) -> Decimal:
    try:
        return Decimal(s)
    except (InvalidOperation, Exception) as e:
        raise BadRequestException(f"invalid amount: {s}") from e
