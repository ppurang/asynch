package org.purang.net.http

trait HttpClient[+F[+_]] {
  def execute(req: HttpRequest, timeout: Timeout): F[HttpResponse]
}
