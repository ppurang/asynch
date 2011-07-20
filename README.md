## Quick

    (GET > "http://www.google.com" >> "Accept" `:` "text/html") ~> {...}

That's all it takes to execute a get on google once you have the imports in order (and __yes__ the __parentheses__ are required).

    import org.purang.net.http._
    import scalaz._, Scalaz._
    import org.purang.net.http.ning._

After that the thing to sort out is the function to deal with the aftermath of your call. The easiest thing is to either return the exception or the body of the response depending on which way the call went.

    (GET > "http://www.google.com") ~> {_.fold(_._1,  _._3)}

That is really it. Almost.

## End Details

The result of a 'asynch' call is

    type ExecutedRequest = FailedRequest or AResponse
    // where 'or' is
    // type or[+E, +A] = scalaz.Validation[E, A]

which in plain lingo would be: an executed request can either be a failed request or a request that successfully returns a response. Here are these two types broken down into their constituents. There is a reason even if subtle of why AResponse is not called a SuccessfulRequest: response with status 5xx or 4xx might be construed a failure.

    type FailedRequest =  (Throwable, Request)
    type AResponse = (Status, Headers, Body, Request)

It is time to show a more detailed and perhaps more useful request handling (kinda lifted from riaks)

    (GET > someuri >> someheaders).~>[Throwable or Option[T]] {
      _.fold(
        t => t,
        _ match {
          case (200, _, Some(body), _) => Some(body.asInstanceOf[T]) //ehhhmmm ugly use a view
          case (404, _, _, _) => None
          case x => RiakException("Retreiving key %s in bucket %s.".format(key, bucketUri), x)
        }
      )
    }

In the above example 'FailedRequest' is implicitly turned into a `Failure[Throwable]` the same thing happens with the `RiakException` later on in the `AResponse` handling. The response with status code 200 results in a `Success(Some(value))` while the 404 indicates a `Success(None)`. Every-other status results in a exception being raised (no it isn't thrown yet, a callback can do that if it so wishes).

`Success` and `Failure` are part of scalaz `Validation` abstraction and is used for among other things the great 'fold' method.

## Start Details

Long before the end comes the beginning. Things usually begin with an HTTP method but needn't. For example:

    GET  // not an executable request just yet

that is  followed by a url against which the method needs to be called

    (GET > "http://www.somehost.com")

which in turn maybe followed by any headers in various forms

    //one header
    (GET > "http://www.somehost.com" >> "header-name" `:` "header-value")

    //two headers
    (GET > "http://www.somehost.com" >> "header-name" `:` "header-value" ++  "another" `:` "one")

    //header with multiple values
    (GET > "http://www.somehost.com" >> "Accept" `:` "application/json" ++ "text/html"

    //multiple headers
    (GET > "http://www.somehost.com" >> ("Accept" `:` "application/json" ++ "text/html" ++ "text/plain") ++
        ("Cache-Control" `:` "no-cache") ++
        ("Content-Type" `:` "text/plain")
    )

    //or use some predefined headers (use types to avoid spelling mistakes ruining the user experience)
    (GET > "http://www.somehost.com" >> Accept(ApplicationJson))

or maybe even a body

    POST >  "http://www.somehost.com" >> ContentType(ApplicationJson) >>> """{"juicy":"yes"}"""

    //or skip those headers completely
    POST >  "http://www.somehost.com" >>> """{"juicy":"yes"}"""

Those are about all the ways you can prepare a request before it gets executed.

After you think your are ready to fire off the request just do

    "http://www.host.com" >> Accept(ApplicationJson) ~> {...} // No http-method in the beginning? Defaults to GET.

The end game we already covered in the previous section.


## Middle Earth

In between the beginning and the end there is that pixie that actually does some work done. It is called the  `Executor`

    type Executor = Request => ExecutedRequest

The method `~>` on  `Request` takes two arguments an `ExecutedRequestHandler` and an implicit `Executor`.

Asynch at present comes with one executor based on `"com.ning" % "async-http-client" % "1.6.4"` which you bring into play by using the import

    import org.purang.net.http.ning._

## Why?

By now you would be asking yourself, there are already some very good alternatives in [dispatch](http://dispatch.databinder.net/Dispatch.html) and [blueeyes](https://github.com/jdegoes/blueeyes) so - Why bother?

Learning aspect of API design in scala and just producing some code should never be underestimated. Principle of full information which in-turn allows the developer full control on how to react to a call gone bad was important too. A developer doesn't need to keep the request around till he comes to processing the results of a call as he has access to it when the callback gets called.

Pleasure of creating something is unbeatable too.

## Help/Join

Critique is sought actively. Help will be provided keenly. Contributions are welcome. Install simple build tool 0.10+, fork the repo and get hacking.

## TODOs

Publish artefact to a mvn repo - needed when riaks is there.

## LICENSE

Released under Apache 2.0. LICENSE file exists near this README. Note all the files belonging to this project are automatically under this one LICENSE.

<pre>
Copyright 2011 Piyush Purang

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
</pre>

## Disclaimer

Use at your own risk. See LICENSE for more details.