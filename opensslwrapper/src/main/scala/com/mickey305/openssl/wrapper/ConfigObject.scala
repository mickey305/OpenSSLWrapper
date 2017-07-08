package com.mickey305.openssl.wrapper

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by K.Misaki on 2017/06/03.
  *
  */
object ConfigObject {
  val config: Config = ConfigFactory.load
}
