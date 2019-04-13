package pt.fabm.abola

import io.reactivex.Completable
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.MessageConsumer
import pt.fabm.abola.models.UserRegisterIn

class DaoVerticleTest : AbstractVerticle() {
  companion object{
    val LOGGER:Logger = LoggerFactory.getLogger(DaoVerticleTest::class.java)
  }
  override fun rxStart(): Completable {
    val consumerUserCreate = messageConsumer("dao.user.create")
    return consumerUserCreate.rxCompletionHandler()
  }

  private fun messageConsumer(address: String): MessageConsumer<UserRegisterIn> {
    val eventBus = vertx.eventBus()
    val consumerUserCreate = eventBus
      .consumer<UserRegisterIn>(address)

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
