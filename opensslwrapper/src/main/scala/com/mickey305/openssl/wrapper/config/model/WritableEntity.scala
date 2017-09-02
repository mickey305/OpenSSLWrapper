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

import java.io.{File, PrintWriter}
import java.util.Properties

import com.mickey305.openssl.wrapper.ConfigObject

/**
  * Created by K.Misaki on 2017/06/01.
  *
  */
trait WritableEntity {
  protected def save(obj: Object): Unit ={
    val config = ConfigObject.config
    val prefix = config.getString("model-config.prefix")
    val ext = config.getString("model-config.suffix")
    val headerMsg = config.getString("model-config.header.message")
    val space = config.getString("standard-lib.half-space")
    val empty = config.getString("standard-lib.empty")
    val fields = obj.getClass.getDeclaredFields
    // Propertiesオブジェクトを生成
    val properties = new Properties

    fields.foreach(field => {
      field.setAccessible(true)
      val value = if (field != null && field.get(obj) != null) field.get(obj).toString else empty
      properties.setProperty(field.getName, value) })

    withPrintWriter(prefix + obj.getClass.getSimpleName + ext) { writer =>
      // ファイルを書き込む
      properties.store(writer, obj.getClass.getSimpleName + space + headerMsg) }
  }

  private def withPrintWriter(file: String)(op: PrintWriter => Unit) {
    val separator = File.separator
    var ioPath = new File(getPath).getCanonicalPath
    createFolder(ioPath)
    ioPath = if (ioPath.isEmpty || ioPath.endsWith(separator)) ioPath else ioPath + separator
    val writer = new PrintWriter(ioPath + file)
    try op(writer)
    finally writer.close()
  }

  private def createFolder(folderName: String): Boolean = {
    val folder = new File(folderName)
    if (!folder.exists())
      return folder.mkdirs()
    false
  }

  def save
  protected def getPath: String
}
