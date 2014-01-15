## Prologue

Sbt library dependency

    "org.purang.net" %% "asynch" %"0.3.0" withSources(),

From

    resolvers += "ppurang bintray" at " http://dl.bintray.com/ppurang/maven"

## Quick

    (GET > "http://www.google.com" >> "Accept" `:` "text/html") ~> {...}

That's all it takes to execute a get on google once you have the imports in order (and __yes__ the __parentheses__ are required).

    import org.purang.net.http._
    import scalaz._, Scalaz._
    import org.purang.net.http.ning._

After that the thing to sort out is the function to deal with the aftermath of your call. The easiest is to either return the exception or the body of the response depending on the results of the call.

    (GET > "http://www.google.com") ~> {_.fold(_._1,  _._3)} //_._1 returns a throwable _._3 returns a Some(body)

That is really it. Almost.

## End Details

The result of a 'asynch' call is

    type ExecutedRequest = FailedRequest or AResponse
    // where 'or' is
    // type or[+E, +A] = scalaz.Validation[E, A]

which in plain lingo would be: an executed request can either be a failed request or a request that successfully returns a response. There is a reason even if subtle of why AResponse is not called a SuccessfulRequest: response with status 5xx or 4xx might be construed a failure. Here are these two types broken down into their constituents.

    type FailedRequest =  (Throwable, Request)
    type AResponse = (Status, Headers, Body, Request)

It is time to show a more detailed and perhaps more useful request handling (kinda lifted from riaks)

    (GET > someuri >> someheaders).~>[Throwable or Option[T]] {
      _.fold(
        t => t, //implcitly changed into a Throwable
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

    (POST >  "http://www.somehost.com" >> ContentType(ApplicationJson) >>> """{"juicy":"yes"}""")

    //or skip those headers completely
    (POST >  "http://www.somehost.com" >>> """{"juicy":"yes"}""")

Those are about all the ways you can prepare a request before it gets executed.

After you think your are ready to fire off the request just do

    ("http://www.host.com" >> Accept(ApplicationJson)) ~> {...} // No http-method in the beginning? Defaults to GET.

The end game we already covered in the previous section.


## Middle Earth

In between the beginning and the end there is that pixie that actually gets some work done. It is called the `Executor`

    type Executor = Request => ExecutedRequest

The method `~>` on  `Request` takes two arguments an `ExecutedRequestHandler` and an implicit `Executor`.

Asynch at present comes with one executor based on `"com.ning" % "async-http-client" % "1.6.4"` which you bring into play by using the import

    import org.purang.net.http.ning._

This also allows configuring your own executor (with a proxy for instance or timeouts etc.).

## Why?

By now you would be asking yourself, there are already some very good alternatives in [dispatch](http://dispatch.databinder.net/Dispatch.html) and [blueeyes](https://github.com/jdegoes/blueeyes) so - Why bother?

Learning aspect of API design in scala and just producing some code should never be underestimated. Principle of full information which in-turn allows the developer full control on how to react to a call gone bad was important too. A developer doesn't need to keep the request around till he comes to processing the results of a call as he has access to it when the callback gets called.

Pleasure of creating something is unbeatable too.

## Help/Join

Critique is sought actively. Help will be provided keenly. Contributions are welcome. Install simple build tool 0.10+, fork the repo and get hacking.

## TODOs and Limitations

   * Publish artefact to a mvn repo - needed when riaks is there.
   * Entity bodies can only be strings or types that can implictly be converted to strings.
   * Not asynchronous as the name might suggest though this might change in the future.
   * The only excutor might not be very robust.
   * No Authentication support.

## LICENSE

```scala

licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))

```


## Disclaimer

Use at your own risk. See LICENSE for more details.