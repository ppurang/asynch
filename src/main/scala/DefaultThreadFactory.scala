package org.purang.net.http.ning

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

import org.purang.net.http.debug

final case class DefaultThreadFactory(namePrefix: String = "org.purang.net.http.ning.pool") extends ThreadFactory {

  private val poolNumber: Int = DefaultThreadFactory.globalPoolNumber.getAndIncrement
  private val threadNumber: AtomicInteger = new AtomicInteger(1)

  //based on java.util.concurrent.Executors.DefaultThreadFactory
  val group: ThreadGroup = {
    val s: SecurityManager = System.getSecurityManager
    if (s != null) s.getThreadGroup else Thread.currentThread().getThreadGroup
  }

  def newThread(r: Runnable): Thread = {
    val t = new Thread(group, r, s"$namePrefix-$poolNumber-${threadNumber.getAndIncrement}")
    debug{
      s"new thread ${t.getName}"
    }
    if (!t.isDaemon) t.setDaemon(true)
    if (t.getPriority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY)
    t
  }
}

object DefaultThreadFactory {
  private[DefaultThreadFactory] val globalPoolNumber = new AtomicInteger(1)
}