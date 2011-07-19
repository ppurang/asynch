package org.purang.net

package http


import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{GivenWhenThen, FeatureSpec}

/**
 * 
 * @author Piyush Purang
 */

class HeaderSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers {

  feature("implicit header conversions") {
    scenario("create simple header") {
      given("the import org.purang.net.http._")
      import org.purang.net.http._

      when("""  "Accept" `:` "application/json"  """)
      val header =  "Accept" `:` "application/json"

      then("header is created")
      header should be(HeaderImpl("Accept", "application/json"))
      and("""string representations is "Accept: application/json" """)
      header.toString should be("""Accept: application/json""")
    }

    scenario("create a header with mutiple values") {
      given("the import org.purang.net.http._")
      import org.purang.net.http._

      when("""  "Accept" `:` "application/json" ++ "text/html" """)
      val header =  "Accept" `:`  "application/json" ++ "text/html"

      then("header is created")
      header should be(HeaderImpl("Accept", "application/json" ++ "text/html"))
      and("""string representations is "Accept: application/json, text/html" """)
      header.toString should be("""Accept: application/json, text/html""")
    }


    scenario("create multiple headers") {
      given("the import org.purang.net.http._")
      import org.purang.net.http._

      when("""  multiple headers are cocatenated using '++'  """)
      val headers =   ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      then("multiple headers are created")
      headers should be(Vector(HeaderImpl("Accept",Vector("application/json", "text/html", "text/plain")), HeaderImpl("Cache-Control",Vector("no-cache")), HeaderImpl("Content-Type",Vector("text/plain"))))
    }

    scenario("use predefined objects") {
      given("the import org.purang.net.http._")
      import org.purang.net.http._

      when("""Accept(ApplicationJson) is used""")
      val header = Accept(ApplicationJson)

      then("""Header equals "Accept" `:` "application/json" """)
      header should be("Accept" `:` "application/json")
    }




  }


}