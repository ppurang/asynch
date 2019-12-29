package org.purang.net

package http

import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers

/**
 * 
 * @author Piyush Purang
 */

class HeaderSpec extends AnyFeatureSpec with GivenWhenThen with Matchers {

  Feature("implicit header conversions") {

    Scenario("create simple header") {
      Given("the import org.purang.net.http._")
      import org.purang.net.http._

      When("""  "Accept" `:` "application/json"  """)
      val header =  "Accept" `:` "application/json"

      Then("header is created")
      header should be(HeaderImpl("Accept", "application/json"))
      And("""string representations is "Accept: application/json" """)
      header.toString should be("""Accept: application/json""")
    }

    Scenario("create a header with mutiple values") {
      Given("the import org.purang.net.http._")
      import org.purang.net.http._

      When("""  "Accept" `:` "application/json" ++ "text/html" """)
      val header =  "Accept" `:`  "application/json" ++ "text/html"

      Then("header is created")
      header should be(HeaderImpl("Accept", "application/json" ++ "text/html"))
      And("""string representations is "Accept: application/json, text/html" """)
      header.toString should be("""Accept: application/json, text/html""")
    }


    Scenario("create multiple headers") {
      Given("the import org.purang.net.http._")
      import org.purang.net.http._

      When("""  multiple headers are cocatenated using '++'  """)
      val headers =   ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      Then("multiple headers are created")
      headers should be(Vector(HeaderImpl("Accept",Vector("application/json", "text/html", "text/plain")), HeaderImpl("Cache-Control",Vector("no-cache")), HeaderImpl("Content-Type",Vector("text/plain"))))
    }

    Scenario("use predefined objects") {
      Given("the import org.purang.net.http._")
      import org.purang.net.http._

      When("""Accept(ApplicationJson) is used""")
      val header = Accept(ApplicationJson)

      Then("""Header equals "Accept" `:` "application/json" """)
      header should be("Accept" `:` "application/json")
    }

  }
}