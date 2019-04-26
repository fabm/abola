package pt.fabm.abola.rest

import io.vertx.core.json.JsonObject


class AppException(
  val args: JsonObject?,
  override val message: String? = null,
  val code: Int = 200
) : RuntimeException()
