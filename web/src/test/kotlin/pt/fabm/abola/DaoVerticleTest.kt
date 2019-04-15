package pt.fabm.abola

import io.reactivex.Completable
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.MessageConsumer
import pt.fabm.abola.models.UserRegisterIn

class DaoVerticleTest : AbstractVerticle() {
  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(DaoVerticleTest::class.java)
  }

  override fun rxStart(): Completable {
    val consumerUserCreate = messageConsumer<UserRegisterIn>("dao.user.create")
    val consumerLoginUser = messageConsumer<JsonObject>("dao.user.login")
    return Completable.concatArray(
      consumerUserCreate.rxCompletionHandler(),
      consumerLoginUser.rxCompletionHandler()
    )
  }

  private fun <T> messageConsumer(address: String): MessageConsumer<T> {
    val eventBus = vertx.eventBus()
    val consumerUserCreate = eventBus
      .consumer<T>(address)

    consumerUserCreate.handler { message ->
      eventBus.rxSend<String>("test.$address", message.body())
        .subscribe({ fw ->
          message.reply(fw.body())
        }, { error ->
          LOGGER.error("Error on message:$address", error)
        })
    }
    return consumerUserCreate
  }
}
