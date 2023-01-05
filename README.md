[![Build Status](https://travis-ci.com/ppurang/asynch.svg?branch=scala3)](https://travis-ci.com/ppurang/asynch)

[![Maven Central](https://img.shields.io/maven-central/v/org.purang.net/asynch_3)](https://search.maven.org/#search|ga|1|org.purang.net.asynch)

# Asynch - Http clients made easy peasy

Check quickstart below, or this [Main.scala](https://github.com/ppurang/asynch/blob/main/src/main/scala-3/org/purang/net/http/Main.scala) for a `Scala 3` flavoured example.

For further examples checkout the tests.

**Note**: Previous versions are available on branch `0.7.x`

## Version

For latest version check the maven badge above. For other versions check: https://search.maven.org/search?q=org.purang.net  or https://repo1.maven.org/maven2/org/purang/net/

```scala
    libraryDependencies += "org.purang.net" %% "asynch" % "3.2.1" //we tend to follow scala 3's version if possible
```

## Quick start

Checkout `Main.scala`

```scala
import org.purang.net.http.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.{
  DefaultAsyncHttpClientConfig,
  DefaultAsyncHttpClient,
  AsyncHttpClient => UnderlyingHttpClient
}

import cats.data.NonEmptyChain
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.show._

import java.util.concurrent.TimeUnit

@main def start(): Unit =
val req = GET > "https://httpbin.org/delay/0" >> Headers(NonEmptyChain(Accept(ApplicationJson)))

val config = new DefaultAsyncHttpClientConfig.Builder()
  .setCompressionEnforced(true)
  .setConnectTimeout(500)
  .setRequestTimeout(3000)
  .setCookieStore(null)
  .build()

val underlyingclient: UnderlyingHttpClient = new DefaultAsyncHttpClient(config)

println((for {
  c <- AsyncHttpClient.sync[IO](
    underlyingclient
  )
  r <- c.execute(
    req,
    Timeout(2000, TimeUnit.MILLISECONDS)
  )
} yield r.show).attempt.unsafeRunSync())
```

## Help/Join

Critique is sought actively. Help will be provided keenly. Contributions are welcome. Install simple build tool 1+, fork the repo and get hacking.

## LICENSE

```scala
licenses += ("BSD", url("http://www.tldrlegal.com/license/bsd-3-clause-license-%28revised%29"))
```

## Disclaimer

Use at your own risk. See LICENSE for more details.
