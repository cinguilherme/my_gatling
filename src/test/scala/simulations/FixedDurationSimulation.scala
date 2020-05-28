package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class FixedDurationSimulation extends Simulation{

  private def getProperty(propertyName: String, defaultValue:String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  def userCount: Int = getProperty("USERS", "5").toInt
  def duration: Int = getProperty("RAMP_DUR", "10").toInt
  def testDuration: Int = getProperty("DUR", "60").toInt

  before(
    println(s"running tests with ${userCount} " +
      s"for the duration of $testDuration with ramp duration of $duration")
  )

  val httpConf = http.baseUrl("http://localhost:8080/app/")

  import scala.concurrent.duration._

  def getAllVg = {
    exec(http("get all vg")
        .get("videogames").check(status.is(200)))
  }

  def getSpecific = {
    exec(http("get specific")
      .get("videogames/2").check(status.is(200)))
  }

  val scn = scenario("RampUsers")
    .forever() {
      exec(getAllVg).pause(1 second)
      .exec(getSpecific).pause(1 second)
      .exec(getAllVg).pause(1 second)
    }

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      rampUsers(userCount) during(duration seconds)
    ).protocols(httpConf.inferHtmlResources())
  ).maxDuration(testDuration seconds)

}
