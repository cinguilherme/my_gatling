package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RampUsersLoadSimulation extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")

  import scala.concurrent.duration._
  val scn = scenario("RampUsers")
    .exec(
      http("get all vg")
        .get("videogames").check(status.is(200)))
    .pause(1 second)
    .exec(http("get specific")
      .get("videogames/2").check(status.is(200)))
    .pause(1 second)
    .exec(
      http("get all vg 2")
        .get("videogames").check(status.is(200)))
    .pause(1 second)

  setUp(
    scn.inject(
      nothingFor(5 seconds),
//      constantUsersPerSec(10) during(15 seconds),
      rampUsersPerSec(1) to (60) during(30 seconds)
    )
  ).protocols(httpConf.inferHtmlResources())
}
