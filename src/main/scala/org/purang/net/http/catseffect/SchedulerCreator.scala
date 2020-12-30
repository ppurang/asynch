package org.purang.net.http.catseffect

import java.util.concurrent.{Executors, ScheduledExecutorService, ThreadFactory}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect._
import cats.effect.unsafe.Scheduler
import cats.effect.unsafe.IORuntime


trait SchedulerCreator[F[_]] {
  def fromThreadFactory(threadFactory: ThreadFactory): F[(Scheduler, ScheduledExecutorService)]
  def fromScheduledExecutorService(scheduler: ScheduledExecutorService): F[Scheduler]
}

object SchedulerCreator {

  def default[F[_]](implicit S: Sync[F]): F[SchedulerCreator[F]] = {
    S.delay {
      new SchedulerCreator[F] {
        override def fromThreadFactory(threadFactory: ThreadFactory): F[(Scheduler, ScheduledExecutorService)] = {
          for {
            ses <- S.delay(Executors.newSingleThreadScheduledExecutor(threadFactory))
            s = Scheduler.fromScheduledExecutor(ses)
          } yield (s, ses)
        }

        override def fromScheduledExecutorService(scheduler: ScheduledExecutorService): F[Scheduler] = S.delay(Scheduler.fromScheduledExecutor(scheduler)) 
      }
    }
  }
}

