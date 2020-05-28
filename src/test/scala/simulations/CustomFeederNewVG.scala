package simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.core.scenario.Simulation
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.io.Source
import scala.util.Random

class CustomFeederNewVG extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  val idNumber = (11 to 20).iterator
  val random = new Random()
  val now = LocalDate.now()
  val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  def randomString(length: Int) = {
    random.alphanumeric.filter(_.isLetter).take(length).mkString
  }

  def getRandomDate(startDate: LocalDate, random: Random) = {
    startDate.minusDays(random.nextInt(30)).format(pattern)
  }

  val modernFeeder = Iterator.continually(Map(
    "gameId" -> random.nextInt(20),
    "name" -> ("Game-"+randomString(5)),
    "review" -> random.nextInt(100).toString,
    "date" -> getRandomDate(now, random),
    "category" -> ("Category-"+randomString(10)),
    "rating" -> ("Rating-"+randomString(4))
  ))

  def postNewGame() = {
    repeat(5) {
      feed(modernFeeder)
        .exec(http("Post new Game")
        .post("videogames/")
        .body(ElFileBody("bodies/newGameTemplate.json")).asJson
          .check(status.is(200)))

    }
  }

  val scn = scenario("create new vg")
      .exec(postNewGame())

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)

}

