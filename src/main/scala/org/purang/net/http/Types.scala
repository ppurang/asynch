package org.purang.net.http

import cats.data.NonEmptyChain
import cats.Show
import cats.syntax.foldable._
import cats.syntax.either._

import java.util.concurrent.TimeUnit
import scala.util.control.NoStackTrace
import scala.concurrent.duration._
import scala.jdk.javaapi.DurationConverters.toJava

final case class Headers(hs: NonEmptyChain[Header]) extends AnyVal

object Headers {
  implicit val show: Show[Headers] = show(Constants.CRLF)

  def show(seperator: String): Show[Headers] = Show.show {
    _.hs.mkString_(seperator)
  }
}

final case class Body(b: String) extends AnyVal

object Body {
  implicit val show: Show[Body] = Show.show {
    _.b
  }
}

final case class Url(url: String) extends AnyVal

object Url {
  implicit def unapply(url: Url): String = url.url

  implicit val show: Show[Url] = Show.show(_.url)
}

final case class Timeout(length: Long, unit: TimeUnit) {
  lazy val msJDuration: java.time.Duration  =
    java.time.Duration.ofMillis(if (unit == TimeUnit.MILLISECONDS) length else unit.toMillis(length))
  lazy val toFiniteDuration: FiniteDuration = FiniteDuration(length, unit)
}

final case class !!!(s: String) extends AssertionError(s) with NoStackTrace
