package org.purang.net

package http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}
import scalaz._
import Scalaz._


/**
 *
 * @author Piyush Purang
 */

class ExecutorSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {

  import org.purang.net.http.ning._

  val bodyOnly: (Status, Headers, Body, Request) => String =
    (status: Status, headers: Headers, body: Body, req: Request) => body.getOrElse("")

  def printResponse(executedRequest: ExecutedRequest) =
    println(executedRequest.fold(responseFailureToString, bodyOnly))

  feature("executor") {

    scenario("executes a request") {
      given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      when("it is executed")
      then("status is not -1")
      (url >> headers) ~> ((x: ExecutedRequest) => x.fold(
        t => -1,
        (status: Status, headers: Headers, body: Body, req: Request) => status
      )) should be(302)

    }

    scenario("executes a more complicated request") {
      given("a request")
      val url = "http://www.google.com"
      val headers = ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
              ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      when("it is executed")
      (HEAD > url >> headers) ~> ((x: ExecutedRequest) => { x.fold(
        (t:Throwable, _:Request) => t.printStackTrace,
        x => {
         x match  {
           case (302, rheaders, _, _) =>
             (GET > rheaders.filter(_.name.equals("Location"))(0).value >> headers) ~>
                     printResponse
           case _ => bodyOnly(x)
         }
       })
      })
    }

   scenario("executes a request after modifying it and returns a value") {
      given("a request")
      val url = "http://www.google.com"
      val contentType = ContentType(ApplicationJson)

      implicit val reqWithApplicationJson : RequestModifier = (req:Request) => req >> contentType

      when("it is executed")
     import Request._
      val headersWereModified = (HEAD, url) ~> {
        (x:ExecutedRequest) => x.fold(
          t => false,
          (status:Status, headers:Headers, body:Body, req:Request) => {println(req.headers);req.headers.contains(contentType)}
        )
      }

      then("Status is returned")
      headersWereModified should be (true)
   }


  }

}