package pt.fabm.abola.rest

import io.vertx.ext.auth.User
import io.vertx.reactivex.SingleHelper
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.auth.AuthProvider
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.models.UserRegisterIn
import java.security.MessageDigest

class UserService(val vertx: Vertx, val md: MessageDigest) {

  val authProvider = AuthProvider.newInstance { authInfo, resultHandler ->
    vertx.eventBus()
      .rxSend<User>("dao.user.login", authInfo)
      .map { it.body() }
      .subscribe(SingleHelper.toObserver<User>(resultHandler))
  }

  fun createUser(rc: RoutingContext) {
    val response = rc.response()
    val bodyAsJson = rc.bodyAsJson.checkedParam("body", ParameterType.ENTRY)
    val name: String = bodyAsJson.checkedString("name", ParameterType.ENTRY)
    val email: String = bodyAsJson.checkedString("email", ParameterType.ENTRY)
    val password: String = bodyAsJson.checkedString("password", ParameterType.ENTRY)
    val token: String = bodyAsJson.checkedString("token", ParameterType.ENTRY)

    val userRegister = UserRegisterIn(
      name,
      email,
      md.digest(password.toByteArray()),
      token
    )

    vertx.eventBus().rxSend<String>("dao.user.create", userRegister)
      .subscribeRest(response) { message ->
        response.statusCode = 204
        response.end()
      }
  }
}
