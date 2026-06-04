package presentation

type BadRequestError struct {
	Message string
}

func (e *BadRequestError) Error() string {
	return e.Message
}

func newBadRequest(msg string) error {
	return &BadRequestError{Message: msg}
}

func IsBadRequestError(err error) (*BadRequestError, bool) {
	e, ok := err.(*BadRequestError)
	return e, ok
}
