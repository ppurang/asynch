package org.purang.net

package http

import org.scalatest.{Matchers, GivenWhenThen, FeatureSpec}
import scalaz._
import Scalaz._
import collection.immutable.Vector
import java.util.concurrent.TimeoutException

class NonBlockingExecutorSpec extends FeatureSpec with GivenWhenThen with Matchers {

  val contentType = ContentType(ApplicationJson)

  val bodyOnly: (Status, Headers, Body, Request) => String =
    (status: Status, headers: Headers, body: Body, req: Request) => body.getOrElse("")

  feature("non blocking executor") {
    scenario("allows timeout") {
      import ning._
      Given("a request")
      val url = "http://www.google.com"
      val contentType = ContentType(ApplicationJson)
      import scalaz.concurrent.Strategy._

      When("it is executed with 0 ms timeout")
      val timeout = (HEAD > url).~>>(0)

      Then("a timeout is raised")
      timeout.attemptRun.fold(_.isInstanceOf[TimeoutException], _ => false) should be(true)
    }

    scenario("executes requests that compose using promises") {
      Given("three requests")
      type Collectors = Int
      type Time = Long
      case class Apples(n: Int)
      object Apples_ {
        def fromJson(str: String): Apples = Apples(str.toInt)
      }
      case class JuiceCartons(n: Int)
      object JuiceCartons_ {
        def fromJson(str: String): JuiceCartons = JuiceCartons(5)
      }

      case class FruitGatherers(n: Collectors) {
        def gather() = Apples(n * 10)
      }

      case class ApplePress(n: Int) {
        def juiceCartons(apples: Apples) = JuiceCartons(apples.n / n)
      }


      val headers: Vector[Header] = Vector[Header]()
      implicit val testExecutor = TestExecutor(
        Map(
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10 * 2).toString), (POST > "http://localhost:8080/apple/garden" >>> "10")).right,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (200, headers, Some(5.toString), (POST > "http://localhost:8080/apple/press" >>> "20")).right,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString), (POST > "http://localhost:8080/person/dude/juice" >>> "5")).right
        )
      )

      val f: FruitGatherers => Apples = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>>().attemptRun.fold(
          t => Apples(0),
          p => p match {
            case (200, _, Some(body), _) => Apples_.fromJson(body)
            case _ => Apples(0)
          }
        )

      val g: Apples => JuiceCartons = apples => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>>().attemptRun.fold(
          t => JuiceCartons(0),
          p => p match {
            case (200, _, Some(body), _) => JuiceCartons_.fromJson(body)
            case _ => JuiceCartons(0)
          }
        )


      val h: JuiceCartons => Time = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>>().attemptRun.fold(
          t => 0,
          p => p match {
            case (200, _, Some(body), _) => body.toLong
            case _ => 10
          }
        )

      When("it is executed")
      val time: Time = h(g(f(FruitGatherers(10))))

      Then("5000 ms are returned")
      time should be(5000)
   }

    scenario("executes requests that compose using promises gBad") {
      Given("three requests")
      type Collectors = Int
      type Time = Long
      case class Apples(n: Int)
      object Apples_ {
        def fromJson(str: String): Apples = Apples(str.toInt)
      }
      case class JuiceCartons(n: Int)
      object JuiceCartons_ {
        def fromJson(str: String): JuiceCartons = JuiceCartons(5)
      }

      case class FruitGatherers(n: Collectors) {
        def gather() = Apples(n * 10)
      }

      case class ApplePress(n: Int) {
        def juiceCartons(apples: Apples) = JuiceCartons(apples.n / n)
      }


      val headers: Vector[Header] = Vector[Header]()
      implicit val testExecutor = TestExecutor(
        Map(
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10 * 2).toString), (POST > "http://localhost:8080/apple/garden" >>> "10")).right,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (new Exception("oho!"), (POST > "http://localhost:8080/apple/press" >>> "20")).left,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString), (POST > "http://localhost:8080/person/dude/juice" >>> "5")).right
        )
      )

      val f: FruitGatherers => Apples = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>>().attemptRun.fold(
          t => Apples(0),
          p => p match {
            case (200, _, Some(body), _) => Apples_.fromJson(body)
            case _ => Apples(0)
          }
        )

      val gBad: Apples => JuiceCartons = apples =>  (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>>().attemptRun.fold(
                t => JuiceCartons(0),
                p => p match {
                  case (200, _, Some(body), _) => JuiceCartons_.fromJson(body)
                  case _ => JuiceCartons(0)
                }
              )

      val h: JuiceCartons => Time = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>>().attemptRun.fold(
          t => 0,
          p => p match {
            case (200, _, Some(body), _) => body.toLong
            case _ => 10
          }
        )

      When("it is executed")
      val time: Time = h(gBad(f(FruitGatherers(10))))

      Then("0 ms are returned")
      time should be(0)
    }
  }
}