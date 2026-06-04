package folio.codinginterview.domain

final case class UserId(value: String) {
  require(value.nonEmpty, "userId must not be empty")
}
