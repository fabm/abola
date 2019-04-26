package pt.fabm.abola.rest

import io.netty.buffer.Unpooled
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.RoutingContext

data class RestResponse(
  val buffer: Buffer? = null,
  val statusCode: Int? = 200
) {
  fun handle(rc: RoutingContext) {
    val response = rc.response()
    if (statusCode != null) {
      response.statusCode = statusCode
    }
    response.end(buffer ?: Buffer.newInstance(io.vertx.core.buffer.Buffer.buffer(Unpooled.EMPTY_BUFFER)))
  }
}
