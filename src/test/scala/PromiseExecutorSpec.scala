package org.purang.net

package http


import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}
import scalaz._
import Scalaz._
import scalaz.concurrent._
import scalaz.concurrent.Strategy._
import collection.immutable.Vector


/**
 *
 * @author Piyush Purang
 */

class PromiseExecutorSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {


  val bodyOnly: (Status, Headers, Body, Request) => String =
    (status: Status, headers: Headers, body: Body, req: Request) => body.getOrElse("")


  feature("executor") {

   /* scenario("executes a request that returns a promise") {
      Given("a request")
      val url = "http://www.google.com"
      val contentType = ContentType(ApplicationJson)
      implicit val reqWithApplicationJson: RequestModifier = (req: Request) => req >> contentType

      When("it is executed")
      import Request.apply
      val promiseOfHeadersWereModified = (HEAD, url) ~>> {
        (x: ExecutedRequest) => x.fold(
          t => false,
          (status: Status, headers: Headers, body: Body, req: Request) => {
            req.headers.contains(contentType)
          }
        )
      }

      Then("Status is returned")
      promiseOfHeadersWereModified() should be(true)
    } */

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

      val f: FruitGatherers => Promise[Apples] = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>> {
        _.fold(
          t => Apples(0),
          p => p match {
            case (200, _, Some(body), _) => Apples_.fromJson(body)
            case _ => Apples(0)
          }
        )
      }

      val g: Apples => Promise[JuiceCartons] = apples => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>> {
        _.fold(
          t => JuiceCartons(0),
          p => p match {
            case (200, _, Some(body), _) => JuiceCartons_.fromJson(body)
            case _ => JuiceCartons(0)
          }
        )
      }

      val h: JuiceCartons => Promise[Time] = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>> {
        _.fold(
          t => 0,
          p => p match {
            case (200, _, Some(body), _) => body.toLong
            case _ => 10
          }
        )
      }

      When("it is executed")
      val time: Promise[Time] = f(FruitGatherers(10)) flatMap g flatMap h

      Then("5000 ms are returned")
      time.get should be(5000)
    }

    scenario("executes requests that compose using promises gGood") {
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

      val f: FruitGatherers => Promise[Apples] = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>> {
        _.fold(
          t => Apples(0),
          p => p match {
            case (200, _, Some(body), _) => Apples_.fromJson(body)
            case _ => Apples(0)
          }
        )
      }

      val gBad: Apples => Promise[JuiceCartons] = apples => throw new AssertionError("something went horribly wrong")

      val gGood: Apples => Promise[JuiceCartons] = apples => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>> {
        _.fold(
          t => JuiceCartons(0),
          p => p match {
            case (200, _, Some(body), _) => JuiceCartons_.fromJson(body)
            case _ => JuiceCartons(0)
          }
        )
      }



      val h: JuiceCartons => Promise[Time] = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>> {
        _.fold(
          t => 0,
          p => p match {
            case (200, _, Some(body), _) => body.toLong
            case _ => 10
          }
        )
      }

      When("it is executed")
      val time: Promise[Time] = f(FruitGatherers(10)) flatMap gGood flatMap h

      Then("5000 ms are returned")
      time.get should be(5000)
    }

/*
    the following will hang for ever .. scalaz promises don't have timeouts!
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
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10 * 2).toString), (POST > "http://localhost:8080/apple/garden" >>> "10")).success,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (200, headers, Some(5.toString), (POST > "http://localhost:8080/apple/press" >>> "20")).success,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString), (POST > "http://localhost:8080/person/dude/juice" >>> "5")).success
        )
      )

      println("---> 01")
      val f: FruitGatherers => Promise[Apples] = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>> {
        _.fold(
          t => Apples(0),
          p => p match {
            case (200, _, Some(body), _) => {      println("---> f");            Apples_.fromJson(body)}
            case _ => Apples(0)
          }
        )
      }
      println("---> 02")

      val gBad: Apples => Promise[JuiceCartons] = apples => {      println("---> g");           throw new AssertionError("something went horribly wrong")         }
      println("---> 03")


      val gGood: Apples => Promise[JuiceCartons] = apples => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>> {
        _.fold(
          t => JuiceCartons(0),
          p => p match {
            case (200, _, Some(body), _) => JuiceCartons_.fromJson(body)
            case _ => JuiceCartons(0)
          }
        )
      }



      val h: JuiceCartons => Promise[Time] = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>> {
        _.fold(
          t => 0,
          p => p match {
            case (200, _, Some(body), _) => {      println("---> h");           body.toLong }
            case _ => 10
          }
        )
      }

      When("it is executed")
      println("---> 04")
      val time: Promise[Time] = f(FruitGatherers(10)) flatMap gBad flatMap h

      Then("5000 ms are returned")
      println("---> 05")
      time() should be(0)
    }*/
  }
}