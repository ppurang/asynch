package org.purang.net.http.catseffect

import cats.effect.unsafe.IORuntime
import cats.effect.Sync

trait IORuntimeCreator[F[_]] {

  def createIORuntime(computePoolThreadNamePrefix: String, blockingECThreadNamePrefix: String, schedulerThreadNamePrefix: String) : F[IORuntime]

}


object IORuntimeCreator {

  import cats.effect.unsafe.IORuntime._

  def default[F[_]](implicit S: Sync[F]): F[IORuntimeCreator[F]] = {
    S.delay {
      new IORuntimeCreator[F] {
        override def createIORuntime(computePoolThreadNamePrefix: String, blockingECThreadNamePrefix: String, schedulerThreadNamePrefix: String): F[IORuntime] = {
          S.delay {
            lazy val global: IORuntime = {
              val (compute, compDown) = createDefaultComputeThreadPool(global, computePoolThreadNamePrefix)
              val (blocking, blockDown) = createDefaultBlockingExecutionContext(blockingECThreadNamePrefix)
              val (scheduler, schedDown) = createDefaultScheduler(schedulerThreadNamePrefix)
              IORuntime(
                compute,
                blocking,
                scheduler,
                () => {
                  compDown()
                  blockDown()
                  schedDown()
                }
              )
            }
            global
          }
        }
      }
    }
  }





}