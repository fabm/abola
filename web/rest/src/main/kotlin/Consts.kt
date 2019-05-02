import io.jsonwebtoken.security.Keys
import java.util.*

object Consts {
  const val ACCESS_TOKEN = "access_token"
  val PASS_PHRASE = loadPassPhrase()
  val SIGNING_KEY = Keys.hmacShaKeyFor(PASS_PHRASE.toByteArray())!!

  private fun loadPassPhrase(): String {
    val props = Properties()
    props.load(Consts::class.java.getResourceAsStream("/app.properties"))
    return props.getProperty("key")
  }
}
