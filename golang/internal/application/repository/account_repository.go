package repository

import "folio/codinginterview/internal/domain"

// AccountRepository は口座管理のリポジトリインターフェースです。
type AccountRepository interface {
	Find(userId domain.UserId) (*domain.Account, error)
	Upsert(userId domain.UserId, account domain.Account) error
	Exists(userId domain.UserId) (bool, error)
}
