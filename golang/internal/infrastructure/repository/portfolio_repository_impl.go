package repository

import (
	"sync"

	apprepository "folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"
)

type PortfolioRepositoryImpl struct {
	mu        sync.RWMutex
	portfolio domain.Portfolio
}

func NewPortfolioRepositoryImpl() apprepository.PortfolioRepository {
	return &PortfolioRepositoryImpl{
		portfolio: domain.MustInitialPortfolio(),
	}
}

func (r *PortfolioRepositoryImpl) Get() (domain.Portfolio, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return r.portfolio, nil
}

func (r *PortfolioRepositoryImpl) Update(portfolio domain.Portfolio) error {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.portfolio = portfolio
	return nil
}
