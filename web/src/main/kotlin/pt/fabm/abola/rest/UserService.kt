package pt.fabm.abola.rest

import io.jsonwebtoken.Jwts
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.handler.impl.JjwtAuthHandlerImp
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Cookie
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.models.UserRegisterIn

class UserService(val vertx: Vertx, val toHash: (String) -> ByteArray) {

  fun userLogin(rc: RoutingContext) {
    val response = rc.response()
    val bodyAsJson = rc.bodyAsJson.checkedParam("body", ParameterType.ENTRY)
      .let { jo ->
        json {
          obj(
            "user" to jo["user"],
            "password" to toHash(jo["password"])
          )
        }
      }

    vertx.eventBus()
      .rxSend<String>("dao.user.login", bodyAsJson)
      .subscribeRest(response) {
        val username = bodyAsJson.getString("user")
        val jws = Jwts.builder().setSubject(username).signWith(JjwtAuthHandlerImp.KEY).compact()
        val cookie = Cookie.cookie(HttpHeaders.AUTHORIZATION.toString(), "Bearer:$jws")
        rc.addCookie(cookie)
        rc.response().end()
      }
  }

  fun createUser(rc: RoutingContext) {
    val response = rc.response()
    val bodyAsJson = rc.bodyAsJson.checkedParam("body", ParameterType.ENTRY)
    val name: String = bodyAsJson.checkedString("name", ParameterType.ENTRY)
    val email: String = bodyAsJson.checkedString("email", ParameterType.ENTRY)
    val password: String = bodyAsJson.checkedString("password", ParameterType.ENTRY)

    val userRegister = UserRegisterIn(
      name,
      email,
      toHash(password)
    )

    vertx.eventBus().rxSend<String>("dao.user.create", userRegister)
      .subscribeRest(response) {
        response.statusCode = 204
        response.end()
      }
  }


}
