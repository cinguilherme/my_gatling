package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class BasicLoadSimulation extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  val scn = scenario("basic load test")
    .exec(
      http("get all vg")
        .get("videogames").check(status.is(200)))
      .exec(http("get specific")
        .get("videogames/2").check(status.is(200))
      )

  import scala.concurrent.duration._
  setUp(
    scn.inject(
      nothingFor(2 seconds),
      atOnceUsers(5),
      rampUsers(10) during(10 seconds)
    ).protocols(httpConf.inferHtmlResources())
  )

}
