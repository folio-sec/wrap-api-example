package folio.codinginterview.presentation;

public sealed class PresentationException extends RuntimeException
        permits PresentationException.BadRequestException {
    protected PresentationException(String message) {
        super(message);
    }

    public static final class BadRequestException extends PresentationException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
