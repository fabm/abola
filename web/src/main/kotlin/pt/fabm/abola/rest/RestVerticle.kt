package pt.fabm.abola.rest

import io.reactivex.Completable
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.handler.impl.JjwtAuthHandlerImp
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.AuthHandler
import io.vertx.reactivex.ext.web.handler.BodyHandler
import io.vertx.reactivex.ext.web.handler.CookieHandler
import io.vertx.reactivex.ext.web.handler.StaticHandler
import java.security.MessageDigest

class RestVerticle : AbstractVerticle() {

  override fun rxStart(): Completable {
    val port = config().checkedInt("port", ParameterType.CONF)
    val host = config().checkedString("host", ParameterType.CONF)

    val router = Router.router(vertx)
    val webRoot = StaticHandler.create().setWebRoot("public")
    router.route().handler(webRoot)

    val messageDigest = MessageDigest.getInstance("SHA-512")
    val userService = UserService(vertx) { messageDigest.digest(it.toByteArray()) }
    val reservationService = ReservationService()

    val jwtAuth = JjwtAuthHandlerImp(AuthProvider { _, resultHandler ->
      resultHandler.handle(Future.succeededFuture())
    })
    router.route("/api/user").handler(BodyHandler.create()).method(HttpMethod.POST)
      .handler(userService::createUser)
    router.route("/api/user/login").handler(BodyHandler.create()).handler(CookieHandler.create())
      .method(HttpMethod.POST).handler(userService::userLogin)
    router.route("/api/reservation").handler(AuthHandler.newInstance(jwtAuth)).method(HttpMethod.GET)
      .handler(reservationService::reservationList)
    return vertx
      .createHttpServer()
      .requestHandler(router)
      .rxListen(port, host)
      .ignoreElement()
  }

}

