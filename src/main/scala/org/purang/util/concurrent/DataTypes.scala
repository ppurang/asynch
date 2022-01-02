package org.purang.util.concurrent

sealed trait ThreadPriority extends Product with Serializable {
  def ordinal: Int

  lazy val javaThreadPriority = this.ordinal
}

case object min extends ThreadPriority {
  lazy val ordinal: Int = 1
}

case object two extends ThreadPriority {
  lazy val ordinal: Int = 2
}

case object three extends ThreadPriority {
  lazy val ordinal: Int = 3
}

case object four extends ThreadPriority {
  lazy val ordinal: Int = 4
}

case object five extends ThreadPriority {
  lazy val ordinal: Int = 5
}

case object six extends ThreadPriority {
  lazy val ordinal: Int = 6
}

case object seven extends ThreadPriority {
  lazy val ordinal: Int = 7
}

case object eight extends ThreadPriority {
  lazy val ordinal: Int = 8
}

case object nine extends ThreadPriority {
  lazy val ordinal: Int = 9
}

case object max extends ThreadPriority {
  lazy val ordinal: Int = 10
}

sealed trait ThreadType extends Product with Serializable {
  def isDaemon: Boolean
}

case object daemon extends ThreadType {
  lazy val isDaemon: Boolean = true
}

case object user extends ThreadType {
  lazy val isDaemon: Boolean = false
}
