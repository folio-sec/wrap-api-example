from __future__ import annotations

from coding_interview.infrastructure.server.dummy_server import DummyServer


def main() -> None:
    DummyServer.default()
    print("DummyServer started")


if __name__ == "__main__":
    main()
