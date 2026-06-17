package org.purang.net.http

import munit.ScalaCheckSuite
import org.scalacheck.Prop._
import org.scalacheck.{ Test => ScalaCheckTest }
import java.util.concurrent.TimeUnit

class TimeoutProps extends ScalaCheckSuite {

  override protected def scalaCheckTestParameters = ScalaCheckTest.Parameters.defaultVerbose
  property("msCapped doesn't fail") {
    forAll { (n1: Long) =>
      try {
        Timeout(n1, TimeUnit.MILLISECONDS).msJDuration
        true
      } catch {
        case _: Throwable => fail("shouldn't fail")
      }
    }
  }
}
