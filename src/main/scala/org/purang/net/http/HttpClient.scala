package org.purang.net.http

import cats.Show

trait HttpClient[+F[_]] {
  def execute(req: HttpRequest, timeout:Timeout) : F[HttpResponse]
}
