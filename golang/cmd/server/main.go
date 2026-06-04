package main

import (
	"fmt"

	"folio/codinginterview/internal/infrastructure/server"
)

func main() {
	_ = server.NewDefaultDummyServer()
	fmt.Println("DummyServer initialized.")
}
