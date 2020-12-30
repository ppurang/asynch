package org.purang.util.concurrent

enum ThreadPriority {
  // Thread.MIN_PRIORITY (1) ...Thread.NORM_PRIORITY (5) ... Thread.MAX_PRIORITY (10)
  case min, two, three, four, norm, six, seven, eight, nine, max

  lazy val javaThreadPriority = this.ordinal + 1
}

enum ThreadType {
  case daemon, user

  lazy val isDaemon: Boolean = this == daemon
}
