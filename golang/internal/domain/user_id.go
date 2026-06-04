package domain

import "errors"

type UserId struct {
	Value string
}

func NewUserId(s string) (UserId, error) {
	if s == "" {
		return UserId{}, errors.New("userId must not be empty")
	}
	return UserId{Value: s}, nil
}
