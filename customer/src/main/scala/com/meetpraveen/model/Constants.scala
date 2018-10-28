package com.meetpraveen.model

import scala.util.Properties.envOrElse

object Constants {
  //Read properties from environment
  val cassandraUrl = envOrElse("cassandraUrl", "localhost")
  val cassandraPort = envOrElse("cassandraPort", "9142")
}