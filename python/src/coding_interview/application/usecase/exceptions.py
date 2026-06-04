from __future__ import annotations


class UsecaseException(Exception):
    pass


class UserNotFoundError(UsecaseException):
    pass


class UserAlreadyExistsError(UsecaseException):
    pass


class AmountTooSmallError(UsecaseException):
    pass


class InvalidPortfolioError(UsecaseException):
    def __init__(self, reason: str) -> None:
        super().__init__(reason)
        self.reason = reason
