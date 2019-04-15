package pt.fabm.abola.rest

import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.models.Reservation

class ReservationService(val vertx: Vertx) {
  fun reservationList(rc: RoutingContext) {
    val response = rc.response()
    vertx.eventBus().rxSend<Reservation?>("dao.reservation.get", null)
      .subscribeRest(response) { message ->
        val isReserved = message.body() != null
        val startDate = message.body()?.start
        val endDate = message.body()?.end
        response.end(
          json {
            obj(
              "reserved" to isReserved,
              "start" to startDate,
              "end" to endDate
            )
          }.toBuffer().let { buffer -> Buffer.newInstance(buffer) }
        )
      }
  }
}
