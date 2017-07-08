package com.mickey305.openssl.wrapper.model

/**
  * Created by K.Misaki on 2017/06/21.
  *
  */
object OptionKey {
  case object ShareKeyLength extends OptionKey
  case object PrivateKeyLength extends OptionKey
}

sealed abstract class OptionKey {
  val name: String = toString
}
