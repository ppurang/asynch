package org.purang.net

package http


/*import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}
import scala.concurrent._
import scala.concurrent.util._
import scalaz._
import Scalaz._
import collection.immutable.Vector
import scala.concurrent.ExecutionContext
import scala.concurrent.forkjoin.ForkJoinPool
import java.util.concurrent.TimeUnit
*/
/**
 *
 * @author Piyush Purang
 */

class FutureExecutorSpec /*extends FeatureSpec with GivenWhenThen with ShouldMatchers {


  val bodyOnly: (Status, Headers, Body, Request) => String =
    (status: Status, headers: Headers, body: Body, req: Request) => body.getOrElse("")

  def responseFailureToString(t: Throwable, req: Request): String = throwableToString(t)


  def throwableToString(t: Throwable): String = {
    def isSbtClass(name: String) = name.startsWith("sbt") || name.startsWith("xsbt")

    def trimmed(throwable: Throwable, d: Int = 50): String = {
      require(d >= 0)
      val b = new StringBuilder()
      def appendStackTrace(t: Throwable, first: Boolean) {
        val include: StackTraceElement => Boolean =
          if (d == 0)
            element => !isSbtClass(element.getClassName)
          else {
            var count = d - 1
            (_ => {
              count -= 1;
              count >= 0
            })
          }
        def appendElement(e: StackTraceElement) {
          b.append("\tat ")
          b.append(e)
          b.append('\n')
        }
        if (!first)
          b.append("Caused by: ")
        b.append(t)
        b.append('\n')
        val els = t.getStackTrace()
        var i = 0
        while ((i < els.size) && include(els(i))) {
          appendElement(els(i))
          i += 1
        }
      }
      appendStackTrace(throwable, true)
      var c = throwable
      while (c.getCause() != null) {
        c = c.getCause()
        appendStackTrace(c, false)
      }
      b.toString()
    }

    trimmed(t);
  }


  def printResponse(executedRequest: ExecutedRequest) =
    println(executedRequest.fold(responseFailureToString, bodyOnly))

  import org.purang.net.http._


  feature("executor") {

    scenario("executes a request that returns a future") {
      import org.purang.net.http.ning._
      Given("a request")
      val url = "http://www.google.com"
      val contentType = ContentType(ApplicationJson)
      implicit val conforms: RequestModifier = (req: Request) => req >> contentType
      implicit val executionContext = ExecutionContext.fromExecutorService(new ForkJoinPool(10))

      When("it is executed")
      import Request.apply
      val promiseOfHeadersWereModified = (HEAD >  url).~>>[Boolean]{
        (x: ExecutedRequest) => x.fold(
          t => false,
          (status: Status, headers: Headers, body: Body, req: Request) => {
            req.headers.contains(contentType)
          }
        )
      }

      `then`("Status is returned")
      blocking(promiseOfHeadersWereModified, FiniteDuration(10, TimeUnit.MILLISECONDS)) should be(true)
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
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10 * 2).toString), (POST > "http://localhost:8080/apple/garden" >>> "10")).success,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (200, headers, Some(5.toString), (POST > "http://localhost:8080/apple/press" >>> "20")).success,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString), (POST > "http://localhost:8080/person/dude/juice" >>> "5")).success
        )
      )

      val f: FruitGatherers => Future[Apples] = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>> {
        _.fold(
          t => Apples(0),
          p => p match {
            case (200, _, Some(body), _) => Apples_.fromJson(body)
            case _ => Apples(0)
          }
        )
      }

      val g: Apples => Future[JuiceCartons] = apples => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>> {
        _.fold(
          t => JuiceCartons(0),
          p => p match {
            case (200, _, Some(body), _) => JuiceCartons_.fromJson(body)
            case _ => JuiceCartons(0)
          }
        )
      }

      //below the code before didn't type check under 2.10.0 because some code paths were returning ints!

      val h: JuiceCartons => Future[Time] = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>>{
        _.fold(
          t => 0l,
          p => p match {
            case (200, _, Some(body), _) => body.toLong
            case _ => 10l
          }
        )
      }

      When("it is executed")
      val time = f(FruitGatherers(10)) flatMap g flatMap h

      `then`("5000 ms are returned")
      blocking(time, FiniteDuration(100, TimeUnit.MILLISECONDS))   should be(5000)
    }

    /*scenario("executes requests that compose using promises 3") {
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
            case (200, _, Some(body), _) => {
              println("---> f"); Apples_.fromJson(body)
            }
            case _ => Apples(0)
          }
        )
      }
      println("---> 02")

      val gBad: Apples => Promise[JuiceCartons] = apples => {
        println("---> g"); throw new AssertionError("something went horribly wrong")
      }
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
            case (200, _, Some(body), _) => {
              println("---> h"); body.toLong
            }
            case _ => 10
          }
        )
      }

      When("it is executed")
      println("---> 04")
      val time: Promise[Time] = f(FruitGatherers(10)) flatMap gGood flatMap h

      `then`("5000 ms are returned")
      println("---> 05")
      time.to(x => x) should be(5000)
    }

    scenario("executes requests that compose using promises take two") {
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

      val f: FruitGatherers => Promise[Option[Apples]] = gatherers => (POST > "http://localhost:8080/apple/garden" >>> gatherers.n.toString).~>> {
        _.fold(
          t => none,
          p => p match {
            case (200, _, Some(body), _) => Apples_.fromJson(body).some
            case _ => none
          }
        )
      }

      val g: Apples => Promise[Option[JuiceCartons]] = apples => (POST > "http://localhost:8080/apple/press" >>> apples.n.toString).~>> {
        _.fold(
          t => none,
          p => p match {
            case (200, _, Some(body), _) => JuiceCartons_.fromJson(body).some
            case _ => none
          }
        )
      }

      val h: JuiceCartons => Promise[Option[Time]] = juiceCartons => (POST > "http://localhost:8080/person/dude/juice" >>> juiceCartons.n.toString).~>> {
        _.fold(
          t => none,
          p => p match {
            case (200, _, Some(body), _) => body.toLong.some
            case _ => none
          }
        )
      }

      When("it is executed")
      val timeToDrink: Time = for (
        someapples <- f(FruitGatherers(10));
        apples <- someapples;
        somejuicecartons <- g(apples);
        juicecartons <- somejuicecartons;
        sometime <- h(juicecartons);
        time <- sometime
      ) yield time

      Then("5000 ms are returned")
      timeToDrink should be(5000)
    }*/



  }

}                          */