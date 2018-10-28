package com.meetpraveen

import org.slf4j.LoggerFactory
import org.slf4j.Logger
import scala.concurrent.Future

object LogUtils {
  // Support for logging interpolated strings
  implicit class LogEnhancer[T](val logStr: StringContext) extends AnyVal {
    //TODO: [Bonus Points] Add support for all types. Currently the args are limited to string types
    def log(args: String*)(implicit log: Logger) = {
      logStr.raw(args: _*)
      log.debug(logStr.raw(args: _*))
    }
  }
}

// Simple trait for mixing in logger
trait LogContext {
  //EXPLORE: Different kinds of implicits
  //1. implicit val, def
  //2. implicit class - Type Classes
  //3. implicit parameter list
  //4. implicit functions
  //Refer - https://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html
  implicit val log = LoggerFactory.getLogger(this.getClass)
}