package pt.fabm.abola.rest

import io.reactivex.Single
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.core.http.HttpServerResponse

enum class ParameterType {
  /**
   *is an webservice entry, in query parameter or payload
   */
  ENTRY,
  /**
   * is a configuration variable expected in verticle
   */
  CONF;

  val statusCode: Int
    get() = when (this) {
      ENTRY -> 400
      CONF -> 501
    }
  val key: String
    get() = when (this) {
      ENTRY -> "param"
      CONF -> "conf"
    }

}

fun JsonObject.checkedString(key: String, parameterType: ParameterType): String {
  return this.getString(key) ?: throw
  AppException(JsonObject().put(parameterType.key, key), null, parameterType.statusCode)
}

fun JsonObject.checkedInt(key: String, parameterType: ParameterType): Int {
  return this.getInteger(key) ?: throw
  AppException(JsonObject().put(parameterType.key, key), null, parameterType.statusCode)
}

fun JsonObject.checkedJsonObject(key: String, parameterType: ParameterType): JsonObject {
  return this.getJsonObject(key) ?: throw
  AppException(JsonObject().put(parameterType.key, key), null, parameterType.statusCode)
}

fun <T> T?.checkedParam(name: String, parameterType: ParameterType): T {
  return this ?: throw
  AppException(JsonObject().put(parameterType.key, name), null, parameterType.statusCode)
}

fun <T> Single<T>.subscribeRest(response: HttpServerResponse, onSuccess: (element: T) -> Unit) {
  this.subscribe(onSuccess, { error ->
    if (error is AppException) {
      response.statusCode = error.code
      if (error.args != null) {
        response.end(Buffer.newInstance(error.args.toBuffer()))
      }
    } else {
      response.statusCode = 500
      response.end(Buffer.newInstance(json {
        obj("message" to error.message).toBuffer()
      }))
    }
  })
}
