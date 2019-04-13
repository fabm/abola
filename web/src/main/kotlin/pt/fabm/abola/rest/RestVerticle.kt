package pt.fabm.abola.rest

import io.reactivex.Completable
import io.vertx.core.http.HttpMethod
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.BodyHandler
import io.vertx.reactivex.ext.web.handler.StaticHandler
import java.security.MessageDigest

class RestVerticle : AbstractVerticle() {

  override fun rxStart(): Completable {
    val port = config().checkedInt("port", ParameterType.CONF)
    val host = config().checkedString("host", ParameterType.CONF)

    val router = Router.router(vertx)
    val webRoot = StaticHandler.create().setWebRoot("public")
    router.route().handler(webRoot)

    val md = MessageDigest.getInstance("SHA-512")
    val userService = UserService(vertx, md)

    router.route("/api/user").handler(BodyHandler.create()).method(HttpMethod.POST).handler(userService::createUser)

    return vertx
      .createHttpServer()
      .requestHandler(router)
      .rxListen(port, host)
      .ignoreElement()
  }

}

