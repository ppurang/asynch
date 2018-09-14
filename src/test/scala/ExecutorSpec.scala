package org.purang.net

package http

import org.scalatest._
import scalaz._, Scalaz._
import java.util.concurrent.TimeoutException


/**
 *
 * @author Piyush Purang
 */

class ExecutorSpec extends FeatureSpec with BeforeAndAfterAll with GivenWhenThen with Matchers {
  val contentType = ContentType(ApplicationJson)

  val bodyOnly: (Status, Headers, Body, Request) => String =
    (status: Status, headers: Headers, body: Body, req: Request) => body.getOrElse("")

  def responseFailureToString(t: Throwable, req: Request): String = throwableToString(t)

  implicit val sse = java.util.concurrent.Executors.newScheduledThreadPool(5)

  override protected def afterAll() = {
    super.afterAll()
    sse.shutdownNow()
  }

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

    trimmed(t)
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
      Then("status is 200")
      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {t._1.printStackTrace ;-1},
        (status: Status, headers: Headers, body: Body, req: Request) => status
      )) should be(200)

    }

    scenario("executes a request after modifying it and returns a value") {
      Given("a request")
      val url = "http://www.google.com"

     When("it is executed")
      val headersWereModified = (HEAD > url) ~>({
        (x:ExecutedRequest) => x.fold(
          t => false,
          (status:Status, headers:Headers, body:Body, req:Request) => req.headers.contains(contentType)
        )
      }, modifier = requestModifier(_ >> contentType))

      Then("Status is returned")
      headersWereModified should be (true)
   }
  }

  feature("misbehaving executor") {

    implicit val exec = MisbehavingExecutor(60000) //make it very painful ;)
    val timeout = 500 //oh we have an escape hatch .. hope it works

    scenario("executes a request") {
      Given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      When("it is executed")
      Then("correct exception bubbles up")
      (url >> headers).~>((x: ExecutedRequest) => x.fold(
        t => {if(t._1.isInstanceOf[TimeoutException]) "Timeout" else t._1.getMessage},
        (status: Status, headers: Headers, body: Body, req: Request) => "Succesful"
      ), timeout) should be("Timeout")

    }
  }





}


