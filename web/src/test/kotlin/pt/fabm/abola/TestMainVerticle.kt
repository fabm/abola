package pt.fabm.abola

import io.vertx.core.DeploymentOptions
import io.vertx.junit5.Timeout
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.client.HttpResponse
import io.vertx.reactivex.ext.web.client.WebClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pt.fabm.abola.models.UserRegisterIn
import pt.fabm.abola.rest.RestVerticle
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.concurrent.TimeUnit


@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  val port = 8888
  val host = "localhost"

  @BeforeEach
  fun deployVerticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(MainVerticle(), DeploymentOptions().setConfig(json {
      obj(
        "verticles" to obj(
          "dao" to DaoVerticleTest::class.java.canonicalName,
          "rest" to RestVerticle::class.java.canonicalName
        ),
        "confs" to obj(
          "rest" to obj(
            "port" to 8888,
            "host" to "localhost"
          )
        )
      )
    }), testContext.succeeding<String> { testContext.completeNow() })
  }

  @Test
  @DisplayName("Should call default router")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun serveDefaultHandler(vertx: Vertx, testContext: VertxTestContext) {
    val client = WebClient.create(vertx)
    client.get(port, host, "/index.html").send { response ->
      val result = response.result()
      println(result.bodyAsString())
      testContext.verify {
        assertTrue(result.statusCode() == 200)
        testContext.completeNow()
      }
    }
  }

  @Test
  @DisplayName("Should create an user")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun createUser(vertx: Vertx, testContext: VertxTestContext) {

    val userRegisterIn:UserRegisterIn = UserRegisterIn(
      name = "test-name",
      email = "my@email.com",
      pass = MessageDigest.getInstance("SHA-512").digest("myPass".toByteArray()),
      token = "mytoken"
    )

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val ebConsumer = eventBus
      .consumer<UserRegisterIn>("test.dao.user.create")
      .handler { message ->
        val body = message.body()
        assertEquals(userRegisterIn.email,body.email)
        assertEquals(userRegisterIn.name,body.name)
        assertEquals(userRegisterIn.pass,body.pass)
        assertEquals(userRegisterIn.token,body.token)
      }

/*
      .putHeader(
        HttpHeaders.AUTHORIZATION.toString(),
        "Basic ${Base64.getEncoder().encodeToString("xico:monteiro".toByteArray())}"
      )

*/
    ebConsumer.rxCompletionHandler().subscribe {
      client.post(port, host, "/api/user")
        .rxSendJsonObject(json {
          obj(
            "name" to "testName",
            "email" to "my@email.com",
            "password" to "myPassword",
            "token" to "123"
          )
        })
        .subscribe { response: HttpResponse<Buffer> ->
          testContext.verify {
            assertEquals(204,response.statusCode())
            testContext.completeNow()
          }
        }
    }
  }
}
