package org.purang.net

package http

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

/**
 * 
 * @author Piyush Purang
 */

class RequestSpec extends AnyFeatureSpec with GivenWhenThen with Matchers {

  Feature("implicit request conversions") {

    Scenario("create a request from a string") {
      Given("a string")
      val url = GET > "http://www.google.com"

      Then("it can be coerced to a Request")
      val req: Request = url
      req.body should be(None)
    }

    Scenario("create a request from a string and add headers and a body to it") {
      Given("a url and some headers")
      val url =  "http://www.google.com"
      val headers =  ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      When("headers and body are added")
      val req = GET > url >> headers >>> "some text"

      Then("it is a valid request")
      req.toString should be("""|GET http://www.google.com
                                |Accept: application/json, text/html, text/plain
                                |Cache-Control: no-cache
                                |Content-Type: text/plain
                                |
                                |some text""".stripMargin)
    }

    Scenario("create a request from a tupple") {
      Given("a tuple of method  url and some headers")
      //the following type hint is needed!
      val tuple: Tuple3[Method, Url, Headers] = (GET, "http://www.google.com", Accept("application/json" ++ "text/html" ++ "text/plain"))

      When("org.purang.net.http.Request._ is imported")
      import org.purang.net.http.Request.apply

      Then("the tuple can be coerced to a valid request")
      (tuple >>> "some text").toString should be("""|GET http://www.google.com
                                |Accept: application/json, text/html, text/plain
                                |
                                |some text""".stripMargin)
    }
  }
}