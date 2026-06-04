package repository

import (
	"sync"

	apprepository "folio/codinginterview/internal/application/repository"
	"folio/codinginterview/internal/domain"
)

type AccountRepositoryImpl struct {
	mu    sync.RWMutex
	store map[string]domain.Account
}

func NewAccountRepositoryImpl() apprepository.AccountRepository {
	return &AccountRepositoryImpl{
		store: make(map[string]domain.Account),
	}
}

func (r *AccountRepositoryImpl) Find(userId domain.UserId) (*domain.Account, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	account, ok := r.store[userId.Value]
	if !ok {
		return nil, nil
	}
	return &account, nil
}

func (r *AccountRepositoryImpl) Upsert(userId domain.UserId, account domain.Account) error {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.store[userId.Value] = account
	return nil
}

func (r *AccountRepositoryImpl) Exists(userId domain.UserId) (bool, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	_, ok := r.store[userId.Value]
	return ok, nil
}
