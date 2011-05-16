package org.purang.net

package http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}

/**
 *
 * @author Piyush Purang
 */

class ExecutorSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {

  import org.purang.net.http.ning._

  def printResponse(response: Response) = println(response match {
    case Left(t) => t.printStackTrace
    case Right(x) => println(x)
  })

  feature("executor") {

    scenario("executes a request") {
      given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      when("it is executed")
      then ("status is not -1")
      (url >> headers) ~> ((x: Response) => x match {
        case Left(t) => -1
        case Right((status, _, _)) => status
      }) should not be(-1)

    }

    scenario("executes a more complicated request") {
      given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      when("it is executed")
      val req = HEAD > url >> headers
      req ~> ((x: Response) => x match {
        case Left(t) => t.printStackTrace
        case Right((302, rheaders, _)) => (GET > rheaders.filter(_.name.equals("Location"))(0).value >> headers) ~>
                printResponse
      })
    }

    scenario("executes a request and returns a value") {
      given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      when("it is executed")
      case class Status(n: Int)
      val req = HEAD > url >> headers
      val status: Status = req ~> {
        _ match {
          case Left(t) => Status(-1)
          case Right((n, _, _)) => Status(n)
        }
      }

      then("Status is returned")
      status should be (Status(302))

    }



  }

}