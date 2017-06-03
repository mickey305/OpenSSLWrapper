package com.mickey.openssl.wrapper.config.model

import java.io.{File, PrintWriter}
import java.util.Properties

import com.mickey.openssl.wrapper.ConfigObject

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
