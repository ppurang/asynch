package org.purang.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {

  private static final String THREAD_FACTORY_PREFIX = "o.p.u.c.DTF";//org.purang.util.concurrent.DefaultThreadFactory
  //sequential pool number - answers, How many DefaultThreadFactory pools exist?
  private static final AtomicInteger POOL_NUMBER = new AtomicInteger(0);
  private final ThreadGroup group;
  //sequential thread number for a given pool
  private final AtomicInteger threadNumber = new AtomicInteger(0);
  private final String namePrefix;
  private final Boolean daemon;
  private final int priority;
  private final Thread.UncaughtExceptionHandler  uncaughtExceptionHandler;

  public DefaultThreadFactory(final String prefix,
                              final Boolean daemon,
                              final int priority,
                              final Thread.UncaughtExceptionHandler uncaughtExceptionHandler
                              ) {

      this.group = Thread.currentThread().getThreadGroup();
      this.namePrefix = THREAD_FACTORY_PREFIX + "-"+ prefix + "-" +
                    POOL_NUMBER.getAndIncrement() +
                   "-thread-";
      this.daemon = daemon;
      this.priority = priority;
      this.uncaughtExceptionHandler = uncaughtExceptionHandler;
  }

  public Thread newThread(final Runnable r) {
      Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
      t.setDaemon(daemon);
      t.setPriority(priority);
      t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
      return t;
  }
}