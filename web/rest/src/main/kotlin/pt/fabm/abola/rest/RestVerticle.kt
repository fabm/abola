package pt.fabm.abola.rest

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.http.HttpServerResponse
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import io.vertx.reactivex.ext.web.handler.BodyHandler
import io.vertx.reactivex.ext.web.handler.CookieHandler
import io.vertx.reactivex.ext.web.handler.StaticHandler
import pt.fabm.abola.JjwtAuthHandler
import pt.fabm.abola.extensions.checkedInt
import pt.fabm.abola.extensions.checkedString
import java.security.MessageDigest

class RestVerticle : AbstractVerticle() {

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(RestVerticle::class.java)
  }

  override fun rxStart(): Completable {
    val port = config().checkedInt("port")
    val host = config().checkedString("host")

    val router = Router.router(vertx)
    val webRoot = StaticHandler.create().setWebRoot("public")
    router.route().handler(webRoot)

    val messageDigest = MessageDigest.getInstance("SHA-512")
    val userService = UserService(vertx) { messageDigest.digest(it.toByteArray()) }
    val reservationService = ReservationService(vertx)

    fun jwt(): JjwtAuthHandler = JjwtAuthHandler(this::bufferResolver)

    router.route("/api/user").handler(BodyHandler.create()).method(HttpMethod.POST)
      .handler(userService::createUser)

    router.post("/api/user/login")
      .handler(BodyHandler.create())
      .handler(CookieHandler.create())
      .handler(bufferResolverH(userService::userLogin))

    router.get("/api/reservation")
      .handler(jwt().doOperation(reservationService::reservationList))

    router.post("/api/reservation")
      .handler(BodyHandler.create())
      .handler(jwt().doOperation(reservationService::createReservation))

    return vertx
      .createHttpServer()
      .requestHandler(router)
      .rxListen(port, host)
      .doOnError { LOGGER.error("error!", it) }
      .ignoreElement()
  }

  private fun handleError(response: HttpServerResponse, error: Throwable) {
    if (error is AppException) {
      response.statusCode = error.code
      if (error.args != null) {
        response.end(Buffer.newInstance(error.args.toBuffer()))
      }
    } else {
      LOGGER.error("technical error", error)
      response.statusCode = 500
      response.end(
        Buffer.newInstance(
          jsonObjectOf("message" to error.message).toBuffer()
        )
      )
    }
  }

  private fun bufferResolverH(fn: (RoutingContext) -> Single<RestResponse>): Handler<RoutingContext> {
    return Handler { rc ->
      fn(rc).subscribe({ it.handle(rc) }, { error ->
        handleError(rc.response(), error)
      })
    }
  }

  private fun bufferResolver(rc: RoutingContext, sb: Single<Buffer>) {
    val response = rc.response()
    sb.subscribe({ buffer ->
      response.end(buffer)
    }, { error ->
      handleError(response, error)
    })
  }
}

