package folio.codinginterview.domain;

/** ユーザーIDを表す。 */
public record UserId(String value) {
    public UserId {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("userId must not be empty");
        }
    }
}
