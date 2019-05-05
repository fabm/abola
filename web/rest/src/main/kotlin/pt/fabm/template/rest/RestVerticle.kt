package pt.fabm.template.rest

import io.reactivex.Completable
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.Cookie
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.handler.CookieHandler
import io.vertx.reactivex.ext.web.handler.StaticHandler
import pt.fabm.template.extensions.*
import pt.fabm.template.rest.services.CarController
import pt.fabm.template.rest.services.UserController

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

    val userService = UserController(vertx)
    val carController = CarController(vertx)

    router.post("/api/user").withBody().handlerSRR(userService::createUser)
    router.post("/api/user/login").withCookies().withBody().handlerSRR(userService::userLogin)
    router.get("/api/car").handlerSRR(carController::getCar)
    router.get("/api/car/list").handlerSRR { carController.carList() }
    router.post("/api/car").withBody().authHandler { carController.createCar(it.rc) }

    router.get("/xxx").handler(CookieHandler.create())
    router.get("/xxx").handler {
      val bla = Cookie.cookie("my_cookie", "blabla")
      it.addCookie(bla)
      it.response().end(JsonObject().put("hello","world").let { Buffer.newInstance(it.toBuffer()) })
    }

    return vertx
      .createHttpServer()
      .requestHandler(router)
      .rxListen(port, host)
      .doOnError { LOGGER.error("Http server initialization error!", it) }
      .ignoreElement()
  }

}

