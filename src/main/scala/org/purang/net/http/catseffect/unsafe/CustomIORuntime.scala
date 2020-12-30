package org.purang.net.http.catseffect.unsafe

import cats.effect.unsafe.IORuntime
import cats.effect.Sync
import cats.effect.IO
import org.purang.net.http.catseffect.IORuntimeCreator



object CustomIORuntime {

  implicit val global: IORuntime = create("o.p.n.h.c.u.compute", "o.p.n.h.c.u.io", "o.p.n.h.c.u..schedule")

  def create(computePoolThreadNamePrefix: String,
             blockingECThreadNamePrefix: String,
             schedulerThreadNamePrefix: String): IORuntime = (for {
    rtc <- IORuntimeCreator.default[IO]
    rt <- rtc.createIORuntime(computePoolThreadNamePrefix, blockingECThreadNamePrefix, schedulerThreadNamePrefix)} yield rt).unsafeRunSync()(cats.effect.unsafe.implicits.global)

}




