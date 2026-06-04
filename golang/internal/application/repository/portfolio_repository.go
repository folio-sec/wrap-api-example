package repository

import "folio/codinginterview/internal/domain"

// PortfolioRepository は最適ポートフォリオのリポジトリインターフェースです。
type PortfolioRepository interface {
	Get() (domain.Portfolio, error)
	Update(portfolio domain.Portfolio) error
}
