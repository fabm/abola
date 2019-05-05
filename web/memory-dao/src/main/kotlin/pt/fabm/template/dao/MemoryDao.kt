package pt.fabm.template.dao

import io.reactivex.Completable
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import pt.fabm.template.models.Reservation
import pt.fabm.template.models.UserRegisterIn
import java.security.MessageDigest

class MemoryDao : AbstractVerticle() {
  companion object{
    val LOGGER = LoggerFactory.getLogger(MemoryDao::class.java)
  }

  private val reservations = mutableListOf<Reservation>()
  private val users = mutableMapOf<String, UserRegisterIn>()

  override fun rxStart(): Completable {
    return createMessageConsumers()
  }

  private fun createMessageConsumers(): Completable {
    val eventBus = vertx.eventBus()
    val completeHandeleres = mutableListOf<Completable>()

    completeHandeleres += eventBus.consumer<Reservation>("dao.reservation.create"){ message->
      reservations += message.body()
      message.reply(null)
    }.rxCompletionHandler()

    completeHandeleres += eventBus.consumer<List<Reservation>>("dao.reservation.list"){ message->
      message.reply(reservations)
    }.rxCompletionHandler()

    completeHandeleres += eventBus.consumer<UserRegisterIn>("dao.user.create"){ message->
      val body = message.body()
      users[body.name] = body
      message.reply(null)
    }.rxCompletionHandler()

    completeHandeleres += eventBus.consumer<JsonObject>("dao.user.login"){message->
      val digestPass = { pass: ByteArray ->
        MessageDigest.getInstance("SHA-512").digest(pass)!!
      }

      val body = message.body()
      val current = users.get(body.getString("user"))

      val auth = current?.takeIf {
        val argPass = body.getBinary("pass")
        argPass.contentEquals(it.pass)
      } !=null
      message.reply(auth)
    }.rxCompletionHandler()

    return Completable.merge(completeHandeleres)
  }
}
