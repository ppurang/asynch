package org.purang.net

package http

import org.scalatest.GivenWhenThen
import scalaz._
import Scalaz._

import scala.collection.immutable.Vector
import java.util.concurrent.TimeoutException

import scalaz.concurrent.Task
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

class NonBlockingExecutorSpec extends AnyFeatureSpec with GivenWhenThen with Matchers {

  val contentType = ContentType(ApplicationJson)

  val bodyOnly: (Status, Headers, Body, Request) => String =
    (_: Status, _: Headers, body: Body, _: Request) => body.getOrElse("")

  feature("non blocking executor") {
    scenario("allows timeout") {
      import ning._
      Given("a request")
      val url = "http://www.google.com"

      When("it is executed with 0 ms timeout")
      val timeout = (HEAD > url).~>>(0)

      Then("a timeout is raised")
      timeout.unsafePerformSyncAttempt.fold(_.isInstanceOf[TimeoutException], _ => false) should be(true)
    }

    scenario("executes requests using tasks where all tasks succeed") {
      Given("three requests")
      type Collectors = Int
      case class Apples(n: Int)
      object Apples_ {
        def fromJson(str: String): Apples = Apples(str.toInt)
      }
      case class JuiceCartons(n: Int)
      object JuiceCartons_ {
        def fromJson(): JuiceCartons = JuiceCartons(5)
      }

      case class FruitGatherers(n: Collectors) {
        def gather() = Apples(n * 10)
      }

      val headers: Vector[Header] = Vector[Header]()
      implicit val testExecutor = TestExecutor(
        Map(
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10 * 2).toString), (POST > "http://localhost:8080/apple/garden" >>> "10")).right,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (200, headers, Some(5.toString), (POST > "http://localhost:8080/apple/press" >>> "20")).right,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString), (POST > "http://localhost:8080/person/dude/juice" >>> "5")).right
        )
      )

      val f: FruitGatherers => Task[Apples] = (gatherers: FruitGatherers) => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>>().map({
        case (200, _, Some(body), _) => Apples_.fromJson(body)
        case _ => Apples(0)
      })

      val g: Apples => Task[JuiceCartons] = (apples:Apples) => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>>().map({
        case (200, _, _, _) => JuiceCartons_.fromJson()
        case _ => JuiceCartons(0)
      })

      val h: JuiceCartons => Task[Long] = (juiceCartons: JuiceCartons) => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>>().map({
        case (200, _, Some(body), _) => body.toLong
        case _ => 10
      })

      When("it is executed")
      val task: Task[Long] = for {
        apples <- f(FruitGatherers(10))
        juice <- g(apples)
        time <- h(juice)
      } yield time

      Then("5000 ms are returned")
      task.unsafePerformSyncAttempt should be(\/-(5000))
   }

    scenario("executes requests using tasks where one fails") {
      Given("three requests")
      type Collectors = Int
      case class Apples(n: Int)
      object Apples_ {
        def fromJson(str: String): Apples = Apples(str.toInt)
      }
      case class JuiceCartons(n: Int)
      object JuiceCartons_ {
        def fromJson(): JuiceCartons = JuiceCartons(5)
      }

      case class FruitGatherers(n: Collectors) {
        def gather() = Apples(n * 10)
      }

      val headers: Vector[Header] = Vector[Header]()
      implicit val testExecutor = TestExecutor(
        Map(
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10 * 2).toString), (POST > "http://localhost:8080/apple/garden" >>> "10")).right,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (new Exception("oho!"), (POST > "http://localhost:8080/apple/press" >>> "20")).left,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString), (POST > "http://localhost:8080/person/dude/juice" >>> "5")).right
        )
      )

      val f: FruitGatherers => Task[Apples] = (gatherers: FruitGatherers) => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>>().map({
        case (200, _, Some(body), _) => Apples_.fromJson(body)
        case _ => Apples(0)
      })

      val g: Apples => Task[JuiceCartons] = (apples:Apples) => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>>().map({
        case (200, _, _, _) => JuiceCartons_.fromJson()
        case _ => JuiceCartons(0)
      })

      val h: JuiceCartons => Task[Long] = (juiceCartons: JuiceCartons) => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>>().map({
        case (200, _, Some(body), _) => body.toLong
        case _ => 10
      })

      When("it is executed")
      val task: Task[Long] = for {
        apples <- f(FruitGatherers(10))
        juice <- g(apples)
        time <- h(juice)
      } yield time

      Then("0 ms are returned")
      task.unsafePerformSyncAttempt.fold(
        _ => -1.left,
        _.right
      ) should be(-\/(-1))
    }
  }
}