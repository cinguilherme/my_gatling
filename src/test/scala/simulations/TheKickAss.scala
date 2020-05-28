package simulations

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class TheKickAss extends Simulation {
  val idNumber = (50 to 1505).iterator
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
    "gameId" -> random.nextInt(1020),
    "name" -> ("Game-"+randomString(5)),
    "review" -> random.nextInt(100).toString,
    "date" -> getRandomDate(now, random),
    "category" -> ("Category-"+randomString(10)),
    "rating" -> ("Rating-"+randomString(4))
  ))

  private def getProperty(propertyName: String, defaultValue:String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def userCount = getProperty("USERS", "5").toInt
  def userRamp = getProperty("RAMP", "10").toInt
  def duration = getProperty("RAMP_DUR", "5").toInt seconds
  def testDuration = getProperty("DUR", "20").toInt seconds

  println("running the KickAss Simulation with the following parameters")
  println(s"userCount ${userCount} with ramp to ${userRamp} in $duration")
  println(s"for the duration of $testDuration")

  val getAllGames =
    exec(http("get all games")
    .get("videogames")
    .check(status.is(200))).pause(1 second)

  def getSpecificGame(id: String) =
    exec(http(s"get specif game with id ${id}")
      .get(s"videogames/${id}")
    .check(status.is(200)))

  def createNewGame() = {
    exec(http("create new game")
      .post("videogames/")
    .body(ElFileBody("bodies/newGameTemplate.json")).asJson
      .check(jsonPath("$.status").saveAs("status"))
    )
  }

  def deleteGame(id: String) = {
    exec(http("delete game")
      .delete(s"videogames/${id}")
      .check(status.is(200)))
  }

  //1
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  val scn = scenario("the test")
    .feed(modernFeeder).forever() {
        exec(getAllGames)
          .exec(createNewGame())
          .exec(getSpecificGame("${gameId}"))
          .exec(deleteGame("${gameId}"))
      }

  setUp(
    scn.inject(
      atOnceUsers(10),
      rampUsers(userRamp) during(duration)
    ).protocols(httpConf.inferHtmlResources())
  ).maxDuration(testDuration)

}
