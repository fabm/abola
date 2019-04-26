package pt.fabm.abola.extensions

import io.reactivex.Single
import io.vertx.core.logging.LoggerFactory
import io.vertx.reactivex.ext.web.RoutingContext

val LOGGER = LoggerFactory.getLogger(Single::class.java)

fun <T> Single<T>.subscribeRest(rc: RoutingContext, onSuccess: (element: T) -> Unit) {
  this.subscribe(onSuccess, {
    LOGGER.error("technical error",it)
  })
}
