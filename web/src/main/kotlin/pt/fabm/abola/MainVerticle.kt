package pt.fabm.abola

import io.reactivex.Completable
import io.vertx.core.DeploymentOptions
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import pt.fabm.abola.dao.LocalCodec
import pt.fabm.abola.extensions.checkedJsonObject
import pt.fabm.abola.extensions.checkedString
import pt.fabm.abola.models.Reservation
import pt.fabm.abola.models.SimpleDate
import pt.fabm.abola.models.UserRegisterIn
import pt.fabm.abola.rest.AppException
import pt.fabm.abola.rest.ParameterType

class MainVerticle : AbstractVerticle() {

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(MainVerticle::class.java)
  }

  private fun <T>registerLocalCodec(klass:Class<T>){
    vertx.eventBus().delegate.registerDefaultCodec(klass, LocalCodec(klass))
  }

  private fun registerCodecs() {
    registerLocalCodec(UserRegisterIn::class.java)
    registerLocalCodec(SimpleDate::class.java)
    registerLocalCodec(Reservation::class.java)
  }

  override fun rxStart(): Completable {
    registerCodecs()
    return deployVerticles().doOnError { error ->
      when (error) {
        is AppException -> LOGGER.error(error.args.toString(), error)
        else -> LOGGER.error("Generic error", error)
      }
    }
  }

  private fun deployVerticles(): Completable {
    val verticles = config().checkedJsonObject("verticles", ParameterType.CONF)
    val restVerticle = verticles.checkedString("rest", ParameterType.CONF)
    val daoVerticle = verticles.checkedString("dao", ParameterType.CONF)
    val confs = config().checkedJsonObject("confs", ParameterType.CONF)
    val restConf = confs.checkedJsonObject("rest", ParameterType.CONF)

    return vertx.rxDeployVerticle(daoVerticle).ignoreElement()
      .andThen(vertx.rxDeployVerticle(restVerticle, DeploymentOptions().setConfig(restConf)).ignoreElement())
  }

}
