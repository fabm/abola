package pt.fabm.abola.modules

import io.vertx.reactivex.ext.auth.AuthProvider
import pt.fabm.abola.dao.UserDao

interface App {
  companion object {
    const val APP_MAP = "APP_MAP"
  }

  val authProvider: AuthProvider
}
