package pt.fabm.abola

import Consts
import io.jsonwebtoken.Jwts
import io.vertx.core.DeploymentOptions
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.junit5.Timeout
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.core.json.obj
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.buffer.Buffer
import io.vertx.reactivex.ext.web.client.HttpResponse
import io.vertx.reactivex.ext.web.client.WebClient
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.yaml.snakeyaml.Yaml
import pt.fabm.abola.MainVerticle
import pt.fabm.abola.models.Reservation
import pt.fabm.abola.models.SimpleDate
import pt.fabm.abola.models.UserRegisterIn
import java.io.FileReader
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  var port:Int?=null
  lateinit var host:String

  @BeforeEach
  fun deployVerticle(vertx: Vertx, testContext: VertxTestContext) {
    val configPath = TestMainVerticle::class.java.getResource("/config.yaml").path
    val yaml = Yaml()
    val map:Map<String,Any> = yaml.load(FileReader(configPath))
    map.get("confs").let { it as Map<*,*> }
      .get("rest"). let { it as Map<*,*> }
      .also { portAndHost->
        port = portAndHost.get("port") as Int
        host = portAndHost.get("host") as String
      }

    vertx.rxDeployVerticle(
      MainVerticle(), DeploymentOptions().setConfig(
        JsonObject()
          .put("path", configPath)
      )
    ).subscribe({

      testContext.completeNow()
    }, {
      testContext.failNow(it)
    })
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
    client.get(port!!, host, "/index.html").send { response ->
      val result = response.result()
      println(result.bodyAsString())
      testContext.verify {
        assertEquals(200, result.statusCode())
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
      client.post(port!!, host, "/api/user")
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
  @DisplayName("Should login user")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun checkLogin(vertx: Vertx, testContext: VertxTestContext) {

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val entry = jsonObjectOf(
      "user" to "testUser",
      "password" to "MyPassword"
    )

    val ebConsumer = eventBus
      .consumer<JsonObject>("test.dao.user.login")
      .handler { message ->
        val body = message.body()
        assertEquals(entry.getString("user"), body["user"])
        assertArrayEquals(digestPass(entry["password"]), body.getBinary("password"))
        message.reply(null) // ignored message
      }

    ebConsumer.rxCompletionHandler().subscribe({
      client.post(port!!, host, "/api/user/login")
        .rxSendJsonObject(entry)
        .subscribe { response: HttpResponse<Buffer> ->
          testContext.verify {
            val cookie = response.cookies().filter { cookieName ->
              cookieName.startsWith(Consts.ACCESS_TOKEN + "=")
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
  @DisplayName("Should fail on authentication when tries to show the reservation")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun authenticationFailedShowReservation(vertx: Vertx, testContext: VertxTestContext) {
    val client = WebClient.create(vertx)

    client.get(port!!, host, "/api/reservation")
      .putHeader(HttpHeaders.COOKIE.toString(), "${Consts.ACCESS_TOKEN}=aaa.bbb.ccc")
      .rxSend()
      .subscribe { response: HttpResponse<Buffer> ->
        testContext.verify {
          response.bodyAsJsonObject().also { jsonObject ->
            assertEquals(jsonObjectOf("result" to "login failed"), jsonObject)
          }
          assertEquals(403, response.statusCode())
          testContext.completeNow()
        }
      }

  }


  @Test
  @DisplayName("Should create a reservation")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun createReservation(vertx: Vertx, testContext: VertxTestContext) {

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val now = LocalDateTime.now()
    val startDate = now.plusDays(1)
    val endDate = now.plusDays(2)

    val reservation = Reservation(
      SimpleDate(
        startDate.year,
        startDate.monthValue,
        startDate.dayOfMonth,
        startDate.hour,
        startDate.minute
      ),
      SimpleDate(
        endDate.year,
        endDate.monthValue,
        endDate.dayOfMonth,
        endDate.hour,
        endDate.minute
      )
    )

    val jws = Jwts.builder().setSubject("test-user")
      .signWith(Consts.SIGNING_KEY)
      .compact()

    val ebConsumer = eventBus
      .consumer<Unit>("test.dao.reservation.create")
      .handler { message ->
        assertEquals(reservation, message.body())
        message.reply(null)
      }

    ebConsumer.rxCompletionHandler().subscribe({
      client.post(port!!, host, "/api/reservation")
        .putHeader(HttpHeaders.COOKIE.toString(), "${Consts.ACCESS_TOKEN}=$jws")
        .rxSendJsonObject(
          jsonObjectOf(
            "start" to reservation.start.toString(),
            "end" to reservation.end.toString()
          )
        )
        .subscribe { response: HttpResponse<Buffer> ->
          testContext.verify {

            testContext.completeNow()
          }
        }
    }, { error ->
      testContext.failNow(error)
    })
  }

  @Test
  @DisplayName("Should show reservations")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  @Throws(Throwable::class)
  fun showReservation(vertx: Vertx, testContext: VertxTestContext) {

    val client = WebClient.create(vertx)
    val eventBus = vertx.eventBus()
    val reservation = Reservation(
      SimpleDate(2019, 1, 2, 3, 4),
      SimpleDate(2019, 1, 2, 3, 5)
    )

    val jws = Jwts.builder().setSubject("test-user")
      .signWith(Consts.SIGNING_KEY)
      .compact()

    val ebConsumer = eventBus
      .consumer<Unit>("test.dao.reservation.list")
      .handler { message ->
        message.reply(listOf(reservation), DeliveryOptions().setCodecName("List"))
      }

    ebConsumer.rxCompletionHandler().subscribe({
      client.get(port!!, host, "/api/reservation")
        .putHeader(HttpHeaders.COOKIE.toString(), "${Consts.ACCESS_TOKEN}=$jws")
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
    ).signWith(Consts.SIGNING_KEY).compact()

    var claims = Jwts.parser().requireSubject(username)
      .setSigningKey(Consts.SIGNING_KEY)
      .parseClaimsJws(jws)

    assertEquals("watcher", claims.body["role"])

    claims = Jwts.parser().requireSubject(username)
      .setSigningKey(Consts.SIGNING_KEY)
      .parseClaimsJws(
        "eyJhbGciOiJIUzM4NCJ9" +
          ".eyJzdWIiOiJ1c2VyLW5hbWUiLCJyb2xlIjoid2F0Y2hlciJ9" +
          ".XvfvyCUycZ9nA7i7nmLP173peL1ZjYrCpxvPKGAZGHX1wL1jVEJRaTwfR3qQMROt"
      )

    assertEquals("watcher", claims.body["role"])

    testContext.completeNow()
  }


}

