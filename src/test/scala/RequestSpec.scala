package org.purang.net

package http

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}

/**
 * 
 * @author Piyush Purang
 */

class RequestSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {

  feature("implicit request conversions") {

    scenario("create a request from a string") {
      given("a string")
      val url = GET > "http://www.google.com"

      then("it can be coerced to Request")
      val req: Request = url
      req.body should be(None)
    }


    scenario("create a request from a string and add headers to it") {
      given("a url and some headers")
      val url =  "http://www.google.com"
      val headers =  ("Accept" `:` "application/json" :: "text/html" :: "text/plain") + ("Cache-Control" `:` "no-cache") + ("Content-Type" `:` "text/plain")

      when("headers are made to be sent to the request")

      val req = GET > url >> headers >>> ""

      then("it is a valid request")
      println(req)
    }
  }

}