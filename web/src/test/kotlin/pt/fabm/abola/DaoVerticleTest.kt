package pt.fabm.abola

import io.reactivex.Completable
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.MessageConsumer
import pt.fabm.abola.models.Reservation
import pt.fabm.abola.models.UserRegisterIn

class DaoVerticleTest : AbstractVerticle() {
  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(DaoVerticleTest::class.java)
  }

  override fun rxStart(): Completable {
    return Completable.mergeArray(
      messageConsumer<UserRegisterIn, String>("dao.user.create").rxCompletionHandler(),
      messageConsumer<JsonObject, String>("dao.user.login").rxCompletionHandler(),
      messageConsumer<Unit?, Reservation>("dao.reservation.get").rxCompletionHandler()
    )
  }

  private fun <E, O> messageConsumer(address: String): MessageConsumer<E> {
    val eventBus = vertx.eventBus()
    val consumerUserCreate = eventBus
      .consumer<E>(address)

    consumerUserCreate.handler { message ->
      eventBus.rxSend<O>("test.$address", message.body())
        .subscribe({ fw ->
          message.reply(fw.body())
        }, { error ->
          LOGGER.error("Error on message:$address", error)
        })
    }
    return consumerUserCreate
  }
}
