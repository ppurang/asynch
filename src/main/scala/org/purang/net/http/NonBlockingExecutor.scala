package org.purang.net.http

trait NonBlockingExecutor extends (Timeout => Request => NonBlockingExecutedRequest)