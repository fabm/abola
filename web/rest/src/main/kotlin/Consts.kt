import io.jsonwebtoken.security.Keys

object Consts {
  const val ACCESS_TOKEN = "access_token"
  const val PASS_PHRASE = "Between Madonna and the whore when I lay with you"
  val SIGNING_KEY = Keys.hmacShaKeyFor(PASS_PHRASE.toByteArray())!!
  object URI {
    const val RESERVATION = "/api/reservation"
  }
}
