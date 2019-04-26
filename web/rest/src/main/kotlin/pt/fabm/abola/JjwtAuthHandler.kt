package pt.fabm.abola

import Consts
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders.COOKIE
import io.vertx.ext.web.impl.CookieImpl
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.Cookie
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.rest.AppException

class JjwtAuthHandler(val bufferResolver: (RoutingContext, Single<Buffer>) -> Unit) : Handler<RoutingContext> {

  lateinit var operation: (claims: Jws<Claims>, rc: RoutingContext) -> Single<Buffer>

  override fun handle(rc: RoutingContext) {
    //get cookie header
    val singleBuffer = Observable.just(rc.request().headers().get(COOKIE))
      //get all cookies
      .flatMapIterable { cookieHeader -> ServerCookieDecoder.STRICT.decode(cookieHeader) }
      //transform netty cookies in vertx cookies
      .map { cookie -> Cookie.newInstance(CookieImpl(cookie)) }
      //filter cookies with access token
      .filter { cookie -> cookie.name == Consts.ACCESS_TOKEN }
      //first cookie which matches with access_token name or throws NoSuchElement
      .firstOrError()
      .map { cookie ->
        try {
          Jwts.parser()
            .setSigningKey(Consts.SIGNING_KEY)
            .parseClaimsJws(cookie.value)
        } catch (e: Exception) {
          throw AppException(jsonObjectOf("result" to "login failed"), null, 403)
        }
      }.flatMap { claims -> operation(claims, rc) }

    bufferResolver(rc, singleBuffer)
  }

  fun doOperation(operation: (claims: Jws<Claims>, rc: RoutingContext) -> Single<Buffer>): JjwtAuthHandler {
    this.operation = operation
    return this
  }

}
