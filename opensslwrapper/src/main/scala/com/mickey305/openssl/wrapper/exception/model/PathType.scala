package com.mickey305.openssl.wrapper.exception.model

/**
  * Created by K.Misaki on 2017/06/20.
  *
  */
object PathType {
  case object Package extends PathType
  case object PrivateKey extends PathType
  case object PublicKey extends PathType
}

sealed abstract class PathType {
  val name: String = toString
}
