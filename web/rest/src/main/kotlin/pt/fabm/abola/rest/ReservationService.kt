package pt.fabm.abola.rest

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.abola.extensions.checkedString
import pt.fabm.abola.models.Reservation
import pt.fabm.abola.models.SimpleDate
import java.time.LocalDateTime

class ReservationService(val vertx: Vertx) {

  fun reservationList(claims: Jws<Claims>, rc: RoutingContext): Single<Buffer> =
    vertx.eventBus().rxSend<List<Reservation>>(
      "dao.reservation.list", null, DeliveryOptions().setCodecName("List")
    )
      .map { message -> message.body() }
      .flatMapObservable { list -> Observable.fromIterable(list) }
      .map { el -> JsonObject().put("start", el.start.toString()).put("end", el.end.toString()) }
      .collect({ JsonArray() }, { jsonArray, element -> jsonArray.add(element) })
      .map { it.toBuffer().let { Buffer.newInstance(it) } }


  /**
   * create a reservation, if there is one interval already reserved throws AppException
   * {"result":"already reserved"}
   * satatusCode 400
   */
  fun createReservation(claims: Jws<Claims>, rc: RoutingContext): Single<Buffer> {
    fun String.asSimpleDate(): SimpleDate {
      return LocalDateTime.parse(this).let {
        SimpleDate(it.year, it.monthValue, it.dayOfMonth, it.hour, it.minute)
      }
    }
    return vertx.eventBus().rxSend<String>(
      "dao.reservation.create",
      Reservation(
        rc.bodyAsJson.checkedString("start").asSimpleDate(),
        rc.bodyAsJson.checkedString("end").asSimpleDate()
      )
    ).map { JsonObject().put("result", "ok").toBuffer() }
      .map { Buffer.newInstance(it) }
  }
}
