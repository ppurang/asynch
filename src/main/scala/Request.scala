package org.purang.net

package http

case class Request(method: Method, url: String, headers: Vector[Header] = Vector(), body: Option[String] = None) {

  def >> (additionalHeaders: Seq[Header]) = copy(headers = headers ++ additionalHeaders)

  def >>> (entity: String) = copy(body = Some(entity))



  def ~>(f: Response => Any)(implicit executor: Request => Response) = f(executor(this))

/*
  TODO Would have been nice to do the following

  def ~>>[T](additionalHeaders: Seq[Header])(implicit f: ResponseHandler[T]) = copy(headers = headers ++ additionalHeaders) ~> f

  def ~>>[T](entity: String)(implicit f: ResponseHandler[T]) = copy(body = Some(entity)) ~> f
*/


  override def toString = method + " " +  url + "\n" + headers.mkString("\n") + "\n" + body.getOrElse("")
}

