package pt.fabm.template.rest.services

import io.reactivex.Observable
import io.reactivex.Single
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import pt.fabm.template.extensions.checkedString
import pt.fabm.template.models.Reservation
import pt.fabm.template.models.SimpleDate
import pt.fabm.template.rest.RestResponse
import java.time.LocalDateTime

class ReservationService(val vertx: Vertx) {

  fun reservationList(): Single<RestResponse> =
    vertx.eventBus().rxSend<List<Reservation>>(
      "dao.reservation.list", null, DeliveryOptions().setCodecName("List")
    )
      .map { message -> message.body() }
      .flatMapObservable { list -> Observable.fromIterable(list) }
      .map { el -> JsonObject().put("start", el.start.toString()).put("end", el.end.toString()) }
      .collect({ JsonArray() }, { jsonArray, element -> jsonArray.add(element) })
      .map { RestResponse(it, 200) }


  /**
   * create a reservation, if there is one interval already reserved throws AppException
   * {"result":"already reserved"}
   * satatusCode 400
   */
  fun createReservation(rc: RoutingContext): Single<RestResponse> {
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
    ).map { JsonObject().put("result", "ok") }
      .map { RestResponse(it,204) }
  }
}
