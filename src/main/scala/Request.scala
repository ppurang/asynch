package org.purang.net

package http

trait Request {
  val url: Url
  val method: Method
  val headers: Headers
  val body: Body
  def >> (additionalHeaders: Headers): Request
  def >>> (newBody: String) : Request
  def ~>[T](f: ResponseHandler[T])(implicit executor: Request => Response) : T
}

object Request {
  def apply(tuple: Tuple4[Url, Method, Headers, Body]) = {
    RequestImpl(tuple._1, tuple._2, tuple._3, tuple._4)
  }
  def apply(tuple: Tuple2[Url, Method]) = {
    RequestImpl(tuple._1, tuple._2)
  }
  def apply(aurl: Url) = {
    RequestImpl(url = aurl)
  }
}

case class RequestImpl(url: Url, method: Method = GET, headers: Headers = Vector(), body: Body = None) extends Request {

  def >> (additionalHeaders: Headers) = copy(headers = headers ++ additionalHeaders)

  def >>> (newBody: String) = copy(body = Option(newBody))

  def ~>[T](f: ResponseHandler[T])(implicit executor: Request => Response) = f(executor(this))


  override def toString = method + " " +  url + "\n" + headers.mkString("\n") + "\n\n" + body.getOrElse("")
}

/*
  TODO Would have been nice to do the following

  def ~>>[T](additionalHeaders: Seq[Header])(implicit f: ResponseHandler[T]) = copy(headers = headers ++ additionalHeaders) ~> f

  def ~>>[T](entity: String)(implicit f: ResponseHandler[T]) = copy(body = Some(entity)) ~> f
*/
