package pt.fabm.abola.extensions

import io.vertx.core.json.JsonObject
import pt.fabm.abola.rest.AppException

fun JsonObject.checkedString(key: String): String {
  return this.getString(key) ?: throw
  AppException(JsonObject().put("param", key), null, 400)
}

fun JsonObject.checkedInt(key: String): Int {
  return this.getInteger(key) ?: throw
  AppException(JsonObject().put("param", key), null, 400)
}

fun JsonObject.checkedJsonObject(key: String): JsonObject {
  return this.getJsonObject(key) ?: throw
  AppException(JsonObject().put("param", key), null, 400)
}
