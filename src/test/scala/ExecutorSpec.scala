package org.purang.net

package http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}
import scalaz._
import Scalaz._
import scalaz.concurrent._
import collection.immutable.Vector


/**
 *
 * @author Piyush Purang
 */

class ExecutorSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {
  val contentType = ContentType(ApplicationJson)



  val bodyOnly: (Status, Headers, Body, Request) => String =
    (status: Status, headers: Headers, body: Body, req: Request) => body.getOrElse("")

  def responseFailureToString(t: Throwable, req: Request): String = throwableToString(t)

  /**
   * The following has been lifted from sbt/xsbt.
   */
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
              count -= 1; count >= 0
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


  feature("executor") {

    import org.purang.net.http.ning._

    scenario("executes a request") {
      Given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      When("it is executed")
      Then("status is not -1")
      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {t._1.printStackTrace ;-1},
        (status: Status, headers: Headers, body: Body, req: Request) => status
      )) should be(302)

    }

    scenario("executes a more complicated request") {
      Given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      When("it is executed")
      (HEAD > url >> headers) ~> ((x: ExecutedRequest) => { x.fold(
        (t:Throwable, _:Request) => t.printStackTrace,
        x => {
         x match  {
           case (302, rheaders, _, _) =>
             (GET > rheaders.filter(_.name.equals("Location"))(0).value >> headers) ~>
                     { _.fold(t => fail(t._1), _ match {case (status,_,_,_) => status should be(200)})}
                     //printResponse
           case x => fail(x)
         }
       })
      })
    }

   scenario("executes a request after modifying it and returns a value") {
      Given("a request")
      val url = "http://www.google.com"

     import MyImplicits.conforms
     When("it is executed")
      val headersWereModified = (HEAD > url) ~> {
        (x:ExecutedRequest) => x.fold(
          t => false,
          (status:Status, headers:Headers, body:Body, req:Request) => req.headers.contains(contentType)
        )
      }

      Then("Status is returned")
      headersWereModified should be (true)
   }

    scenario("executes a request that returns a promise") {
      Given("a request")
      val url = "http://www.google.com"
      val contentType = ContentType(ApplicationJson)
      import scalaz.concurrent.Strategy._
      import MyImplicits.conforms
      When("it is executed")
      val promiseOfHeadersWereModified = (HEAD > url) ~>> {
        (x:ExecutedRequest) => x.fold(
          t => false,
          (status:Status, headers:Headers, body:Body, req:Request) => {
            req.headers.contains(contentType)
          }
        )
      }

      Then("Status is returned")
      promiseOfHeadersWereModified.get should be (true)
    }

  }

  feature("promise executor") {


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
          (POST > "http://localhost:8080/apple/garden" >>> "10") -> (200, headers, Some((10*2).toString),  (POST > "http://localhost:8080/apple/garden" >>> "10")).success,
          (POST > "http://localhost:8080/apple/press" >>> "20") -> (200, headers, Some(5.toString),  (POST > "http://localhost:8080/apple/press" >>> "20")).success,
          (POST > "http://localhost:8080/person/dude/juice" >>> "5") -> (200, headers, Some(5000.toString),  (POST > "http://localhost:8080/person/dude/juice" >>> "5")).success
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

  }

  object MyImplicits {

    implicit val conforms : RequestModifier = (req:Request) => req >> contentType

  }
}