from __future__ import annotations


class PresentationException(Exception):
    pass


class BadRequestException(PresentationException):
    def __init__(self, message: str) -> None:
        super().__init__(message)
        self.message = message
