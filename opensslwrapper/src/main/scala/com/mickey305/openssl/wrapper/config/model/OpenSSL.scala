package com.mickey305.openssl.wrapper.config.model

/**
  * Created by K.Misaki on 2017/06/01.
  *
  */
class OpenSSL extends WritableEntity with ReadableEntity with PrintableEntity
  with Version
  with Algorithm
  with Keys
  with SubCommands
  with Base64 {
  val name = "openssl"
  var path = ""
  var freeSpace = ""
  override var version: String = _
  override var algorithm: String = _
  override var shareKey: String = _
  override var publicKey: String = _
  override var privateKey: String = _
  override var base64: Boolean = _

  override def save: Unit = super.save(this)
  override def load: Unit = super.load(this)
  override def console(sig: Int = 0): Unit = super.console(this, sig)
  override def console: Unit = this.console()
  override protected def getPath: String = this.path
}

protected trait Version {
  var version: String
}

protected trait SubCommands {
  val randValue = "rand" // 共通鍵生成
  val encValue = "enc" // 共有鍵暗号（暗号化／復号）
  val genRsaValue = "genrsa" // 秘密鍵生成
  val rsaValue = "rsa" // 公開鍵生成
  val rsaUtilValue = "rsautl" // 公開鍵暗号（暗号化／復号）
}

protected trait Algorithm {
  var algorithm: String
}

protected trait Keys {
  var shareKey: String
  var publicKey: String
  var privateKey: String
}

protected trait Base64 {
  var base64: Boolean
  val base64Value = "base64"
}
