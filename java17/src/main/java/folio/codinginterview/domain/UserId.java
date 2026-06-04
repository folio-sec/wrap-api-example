package folio.codinginterview.domain;

public record UserId(String value) {
    public UserId {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("userId must not be empty");
        }
    }
}
