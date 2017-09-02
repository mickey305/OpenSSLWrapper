// Copyright (C) 2017 K.Misaki <pro.mickey.12091it@gmail.com>
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
//
//
//
//
//
//
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
