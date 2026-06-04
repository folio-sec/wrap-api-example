package folio.codinginterview.presentation;

public class PresentationException extends RuntimeException {
    protected PresentationException(String message) {
        super(message);
    }

    public static final class BadRequestException extends PresentationException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}
