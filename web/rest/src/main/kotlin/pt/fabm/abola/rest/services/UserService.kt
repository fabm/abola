package pt.fabm.abola.rest.services

import Consts
import io.jsonwebtoken.Jwts
import io.reactivex.Single
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.Cookie
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.extensions.checkedString
import pt.fabm.abola.extensions.toHash
import pt.fabm.abola.models.UserRegisterIn
import pt.fabm.abola.rest.RestResponse

class UserService(val vertx: Vertx) {

  fun userLogin(rc: RoutingContext): Single<RestResponse> {
    val singleBodyAsJson = Single.just(rc)
      .map { it.bodyAsJson }
      .map { jo ->
        jsonObjectOf(
          "user" to jo["user"],
          "pass" to (jo.getString("pass").toHash())
        )
      }

    return singleBodyAsJson.flatMap { bodyAsJson ->
      vertx.eventBus()
        .rxSend<Boolean>("dao.user.login", bodyAsJson)
        .map { message ->
          if(!message.body()){
            return@map RestResponse(statusCode = 403)
          }
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
          password.toHash()
        )

        vertx.eventBus().rxSend<String>("dao.user.create", userRegister)
          .map { RestResponse(statusCode = 200) }
      }
  }


}
