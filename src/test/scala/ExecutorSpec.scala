package org.purang.net

package http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}

/**
 * 
 * @author Piyush Purang
 */

class ExecutorSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {

  feature("executor") {

    scenario("executes a request") {
      given("a request")
      val url =  "http://www.google.com"
      val headers =  ("Accept" `:` "application/json" :: "text/html" :: "text/plain") + ("Cache-Control" `:` "no-cache") + ("Content-Type" `:` "text/plain")

      when("it is executed")
      import org.purang.net.http.ning._
      (GET > url >> headers) ~> ((x:Response) => println(x match {
        case Left(t) => t.printStackTrace
        case Right(x) => println(x)
      }))
    }
  }

}