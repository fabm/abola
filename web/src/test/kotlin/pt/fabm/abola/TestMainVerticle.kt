package pt.fabm.abola

import io.jsonwebtoken.Jwts
import io.vertx.core.DeploymentOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.impl.JjwtAuthHandlerImp
import io.vertx.junit5.Timeout
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.client.HttpResponse
import io.vertx.reactivex.ext.web.client.WebClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pt.fabm.abola.models.Reservation
import pt.fabm.abola.models.UserRegisterIn
import pt.fabm.abola.rest.RestVerticle
import java.security.MessageDigest
import java.time.LocalDateTime
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
    })) { ar ->
      if (ar.succeeded()) {
        testContext.completeNow()
      } else {
        testContext.failNow(ar.cause())
      }
    }
  }

  val digestPass = { pass: String ->
    MessageDigest.getInstance("SHA-512").digest(pass.toByteArray())
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

    val digestPass = { pass: String ->
      MessageDigest.getInstance("SHA-512").digest(pass.toByteArray())
    }

    val entry = json {
      obj(
        "name" to "testName",
        "email" to "my@email.com",
        "password" to "myPassword",
        "token" to "123"
      )
    }

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val ebConsumer = eventBus
      .consumer<UserRegisterIn>("test.dao.user.create")
      .handler { message ->
        val body = message.body()
        assertEquals(entry["email"], body.email)
        assertEquals(entry["name"], body.name)
        assertArrayEquals(digestPass(entry["password"]), body.pass)
        message.reply(null) // ignored message
      }

    ebConsumer.rxCompletionHandler().subscribe {
      client.post(port, host, "/api/user")
        .rxSendJsonObject(entry)
        .subscribe { response: HttpResponse<Buffer> ->
          testContext.verify {
            assertEquals(204, response.statusCode())
            testContext.completeNow()
          }
        }
    }
  }

  @Test
  @DisplayName("login user")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun checkLogin(vertx: Vertx, testContext: VertxTestContext) {

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val entry = json {
      obj(
        "user" to "testUser",
        "password" to "MyPassword"
      )
    }
    val ebConsumer = eventBus
      .consumer<JsonObject>("test.dao.user.login")
      .handler { message ->
        val body = message.body()
        assertEquals(entry.getString("user"), body["user"])
        assertArrayEquals(digestPass(entry["password"]), body.getBinary("password"))
        message.reply(null) // ignored message
      }

    ebConsumer.rxCompletionHandler().subscribe({
      client.post(port, host, "/api/user/login")
        .rxSendJsonObject(entry)
        .subscribe { response: HttpResponse<Buffer> ->
          testContext.verify {
            val cookie = response.cookies().filter {
              it.startsWith(HttpHeaders.AUTHORIZATION.toString() + "=")
            }
            assertEquals(200, response.statusCode())
            assertEquals(1, cookie.size)
            testContext.completeNow()
          }
        }

    }, { error ->
      testContext.failNow(error)
    })
  }

  @Test
  @DisplayName("Should show the reservation")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun showReservation(vertx: Vertx, testContext: VertxTestContext) {

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val reservation = Reservation(
      LocalDateTime.of(2019, 1, 2, 3, 4),
      LocalDateTime.of(2019, 1, 2, 3, 5)
    )

    val jws = Jwts.builder().setSubject("test-user")
      .signWith(JjwtAuthHandlerImp.KEY)
      .compact()

    val ebConsumer = eventBus
      .consumer<Unit?>("test.dao.reservation.get")
      .handler { message ->
        message.reply(reservation)
      }

    ebConsumer.rxCompletionHandler().subscribe({
      client.get(port, host, "/api/reservation")
        .bearerTokenAuthentication(jws)
        .rxSend()
        .subscribe { response: HttpResponse<Buffer> ->
          testContext.verify {
            println(response.body().toString())
            testContext.completeNow()
          }
        }
    }, { error ->
      testContext.failNow(error)
    })
  }


  @Test
  @DisplayName("Should generate a token")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun jwtTest(testContext: VertxTestContext) {
    val username = "user-name"
    val jws = Jwts.builder().setSubject(username).addClaims(
      mapOf("role" to "watcher")
    ).signWith(JjwtAuthHandlerImp.KEY).compact()

    var claims = Jwts.parser().requireSubject(username)
      .setSigningKey(JjwtAuthHandlerImp.KEY)
      .parseClaimsJws(jws)

    assertEquals("watcher", claims.body["role"])

    claims = Jwts.parser().requireSubject(username)
      .setSigningKey(JjwtAuthHandlerImp.KEY)
      .parseClaimsJws(
        "eyJhbGciOiJIUzM4NCJ9" +
          ".eyJzdWIiOiJ1c2VyLW5hbWUiLCJyb2xlIjoid2F0Y2hlciJ9" +
          ".XvfvyCUycZ9nA7i7nmLP173peL1ZjYrCpxvPKGAZGHX1wL1jVEJRaTwfR3qQMROt"
      )

    assertEquals("watcher", claims.body["role"])

    testContext.completeNow()
  }


}




