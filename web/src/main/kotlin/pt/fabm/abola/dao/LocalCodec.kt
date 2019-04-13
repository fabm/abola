package pt.fabm.abola.dao

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.reactivex.core.Vertx
import pt.fabm.abola.models.UserRegisterIn

class LocalCodec <T>(var klass: Class<T>) : MessageCodec<T, T> {
  override fun decodeFromWire(pos: Int, buffer: Buffer?): T {
    throw IllegalStateException("only implemented in remote purposes")
  }

  override fun systemCodecID(): Byte {
    return (-1).toByte()
  }

  override fun encodeToWire(buffer: Buffer?, s: T) {
    throw IllegalStateException("only implemented in remote purposes")
  }

  override fun transform(s: T): T {
    return s
  }

  override fun name(): String {
    return klass.simpleName
  }

}


