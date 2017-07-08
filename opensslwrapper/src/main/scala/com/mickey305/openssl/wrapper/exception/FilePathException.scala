package com.mickey305.openssl.wrapper.exception

import com.mickey305.openssl.wrapper.exception.model.PathType

/**
  * Created by K.Misaki on 2017/06/20.
  *
  */
class FilePathException(msg: String, pathType: PathType) extends Exception(msg) {
  val this.pathType = pathType
}
