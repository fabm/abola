package pt.fabm.abola

import io.reactivex.Completable
import io.vertx.core.DeploymentOptions
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.core.AbstractVerticle
import pt.fabm.abola.dao.LocalCodec
import pt.fabm.abola.models.UserRegisterIn
import pt.fabm.abola.rest.AppException
import pt.fabm.abola.rest.ParameterType
import pt.fabm.abola.rest.checkedJsonObject
import pt.fabm.abola.rest.checkedString

class MainVerticle : AbstractVerticle() {

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(MainVerticle::class.java)
  }

  private fun createCodecs() {
    vertx.eventBus().delegate.registerDefaultCodec(UserRegisterIn::class.java, LocalCodec(UserRegisterIn::class.java))
  }

  override fun rxStart(): Completable {
    createCodecs()
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

    return vertx.rxDeployVerticle(daoVerticle)
      .flatMap { vertx.rxDeployVerticle(restVerticle, DeploymentOptions().setConfig(restConf)) }
      .ignoreElement()
  }

}
