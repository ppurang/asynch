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
      given("need for accept header with a single value")
      import org.purang.net.http._

      when("""  "Accept" `:` "application/json"  """)
      val header =  "Accept" `:` "application/json"

      then("header is created")
      header should be(HeaderImpl("Accept", "application/json"))
      and("""string representations is "Accept: application/json" """)
      header.toString should be("""Accept: application/json""")
    }

    scenario("create multiple header") {
      given("need for accept header with a single value")
      import org.purang.net.http._

      when("""  "Accept" `:` "application/json" :: "text/html" """)
      val header =  "Accept" `:`  "application/json" ++ "text/html"

      then("header is of created")
      header should be(HeaderImpl("Accept", "application/json" ++ "text/html"))
      and("""string representations is "Accept: application/json, text/html" """)
      header.toString should be("""Accept: application/json, text/html""")
    }


    scenario("create multiple headers") {
      given("need for accept header with a single value")

      when("""  "Accept" `:` "application/json"  """)
            import org.purang.net.http._
      val headers =   ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++ ("Cache-Control" `:` "no-cache") ++ ("Content-Type" `:` "text/plain")

      then("header is of type Header")
      headers should be(Vector(HeaderImpl("Accept",Vector("application/json", "text/html", "text/plain")), HeaderImpl("Cache-Control",Vector("no-cache")), HeaderImpl("Content-Type",Vector("text/plain"))))
      //println(headers2.mkString("\n"))

    }




  }


}