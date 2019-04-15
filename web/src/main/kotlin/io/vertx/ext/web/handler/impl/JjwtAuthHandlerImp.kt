package io.vertx.ext.web.handler.impl

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.RoutingContext

internal class JjwtAuthHandlerImp(authProvider: AuthProvider) : AuthorizationAuthHandler(
  authProvider,
  Type.BEARER
) {
  companion object {
    const val PASS_PHRASE = "Between Madonna and the whore when I lay with you"
    val KEY = Keys.hmacShaKeyFor(PASS_PHRASE.toByteArray())
  }

  override fun parseCredentials(context: RoutingContext?, handler: Handler<AsyncResult<JsonObject>>?) {
    parseAuthorization(context, false) { parseAuthorization ->
      parseAuthorization.result()
      if (parseAuthorization.failed()) {

        handler?.handle(Future.failedFuture(parseAuthorization.cause()))
        return@parseAuthorization
      }
      val claims = Jwts.parser()
        .setSigningKey(KEY)
        .parseClaimsJws(parseAuthorization.result())

      handler?.handle(
        Future.succeededFuture(JsonObject().put("role", claims.body.get("role")))
      )
    }
  }

  override fun authenticateHeader(context: RoutingContext?): String = "Bearer"
}
