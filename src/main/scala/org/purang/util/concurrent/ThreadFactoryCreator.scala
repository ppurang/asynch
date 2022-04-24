package org.purang.util.concurrent

import cats.effect._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.purang.util.concurrent.{DefaultThreadFactory, ThreadPriority, ThreadType}

import java.util.concurrent.{ThreadFactory, Executors => ExecutorsJ}

trait ThreadFactoryCreator[F[_]] {
  def threadFactory(
      prefix: String,
      threadType: ThreadType,
      priority: ThreadPriority,
      uncaughtExceptionHandler: Thread.UncaughtExceptionHandler
  ): F[ThreadFactory]
}

object ThreadFactoryCreator {

  def default[F[_]](implicit S: Sync[F]): F[ThreadFactoryCreator[F]] = S.delay {
    new ThreadFactoryCreator[F] {
      override def threadFactory(
          prefix: String,
          threadType: ThreadType,
          priority: ThreadPriority,
          uncaughtExceptionHandler: Thread.UncaughtExceptionHandler
      ): F[ThreadFactory] = S.delay {
        val dtf = new DefaultThreadFactory(
          prefix,
          threadType.isDaemon,
          priority.javaThreadPriority,
          uncaughtExceptionHandler
        )
        // fail fast
        // we check if we can create threads using the new TF if we can't well we are under an F[_] ;)
        val t   = dtf.newThread(new Runnable {
          override def run(): Unit = {
            // done
          }
        })
        require(
          t.getName.contains(prefix),
          s"thread factory created a thread '${t.getName}' without the suggested $prefix"
        )
        dtf
      }
    }
  }
}
