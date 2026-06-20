package folio.codinginterview.domain

/** ユーザーIDを表す。 */
final case class UserId(value: String) {
  require(value.nonEmpty, "userId must not be empty")
}
