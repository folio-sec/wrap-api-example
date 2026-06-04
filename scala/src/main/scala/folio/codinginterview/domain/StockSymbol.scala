package folio.codinginterview.domain

enum StockSymbol {
  case Toyopa, Somy
}

object StockSymbol {
  def fromString(s: String): Option[StockSymbol] = s match {
    case "Toyopa" => Some(Toyopa)
    case "Somy"   => Some(Somy)
    case _        => None
  }
}
