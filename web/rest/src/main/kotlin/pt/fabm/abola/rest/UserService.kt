package pt.fabm.abola.rest

import Consts
import io.jsonwebtoken.Jwts
import io.reactivex.Single
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.Cookie
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.extensions.checkedJsonObject
import pt.fabm.abola.extensions.checkedString
import pt.fabm.abola.models.UserRegisterIn

class UserService(val vertx: Vertx, val toHash: (String) -> ByteArray) {

  fun userLogin(rc: RoutingContext): Single<RestResponse> {
    val singleBodyAsJson = Single.just(rc)
      .map { it.bodyAsJson }
      .map { jo ->
        jsonObjectOf(
          "user" to jo["user"],
          "password" to toHash(jo["password"])
        )
      }

    return singleBodyAsJson.flatMap { bodyAsJson ->
      vertx.eventBus()
        .rxSend<Buffer>("dao.user.login", bodyAsJson)
        .map { message ->
          val username = bodyAsJson.getString("user")
          val jws = Jwts.builder().setSubject(username).signWith(Consts.SIGNING_KEY).compact()
          val cookie = Cookie.cookie(Consts.ACCESS_TOKEN, jws)
          rc.addCookie(cookie)
          message.reply(null)
          RestResponse(statusCode = 200)
        }
    }
  }

  fun createUser(rc: RoutingContext): Single<RestResponse> {
    return Single.just(rc).map { it.response() }
      .flatMap {
        val bodyAsJson = rc.bodyAsJson
        val name: String = bodyAsJson.checkedString("name")
        val email: String = bodyAsJson.checkedString("email")
        val password: String = bodyAsJson.checkedString("password")

        val userRegister = UserRegisterIn(
          name,
          email,
          toHash(password)
        )

        vertx.eventBus().rxSend<String>("dao.user.create", userRegister)
          .map { RestResponse(statusCode = 200) }
      }
  }


}
