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

      then("it can be coerced to a Request")
      val req: Request = url
      req.body should be(None)
    }


    scenario("create a request from a string and add headers and a body to it") {
      given("a url and some headers")
      val url =  "http://www.google.com"
      val headers =  ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      when("headers and body are added")
      val req = GET > url >> headers >>> "some text"

      then("it is a valid request")
      req.toString should be("""|GET http://www.google.com
                                |Accept: application/json, text/html, text/plain
                                |Cache-Control: no-cache
                                |Content-Type: text/plain
                                |
                                |some text""".stripMargin)
    }

    scenario("create a request from a tupple") {
      given("a tuple of method  url and some headers")
      //the following type hint is needed!
      val tuple: Tuple3[Method, Url, Headers] = (GET, "http://www.google.com", Accept("application/json" ++ "text/html" ++ "text/plain"))

      when("org.purang.net.http.Request._ is imported")
      import org.purang.net.http.Request.apply

      then("the tuple can be coerced to a valid request")
      (tuple >>> "some text").toString should be("""|GET http://www.google.com
                                |Accept: application/json, text/html, text/plain
                                |
                                |some text""".stripMargin)
    }
  }
}