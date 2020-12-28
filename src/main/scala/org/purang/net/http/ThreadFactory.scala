package org.purang.net.http

import java.util.concurrent.{ThreadFactory, Executors => ExecutorsJ}

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect._

trait ThreadFactoryCreator[F[_]] {
  def threadFactory(prefix: String, uncaughtExceptionHandler: Thread.UncaughtExceptionHandler): F[ThreadFactory]
}

object ThreadFactory {

  def apply[F[_] : Sync](prefix: String,
                         uncaughtExceptionHandler: Thread.UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler
                        ): F[ThreadFactory] = {
    for {
      counter <- Ref.of[F, Int](1)
      factory <- Sync[F].delay {
        ExecutorsJ.defaultThreadFactory()
      }
    } yield {
      new ThreadFactory {
        def newThread(runnable: Runnable) = {
          val thread = factory.newThread(runnable)
          val threadId = thread.getId
          thread.setName(s"$prefix-$threadId")
          thread.setUncaughtExceptionHandler(uncaughtExceptionHandler)
          thread
        }
      }
    }
  }
}
