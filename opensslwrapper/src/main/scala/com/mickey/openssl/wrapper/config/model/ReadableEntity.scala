package com.mickey.openssl.wrapper.config.model

import java.io.{File, FileReader, Reader}
import java.util.Properties

import com.mickey.openssl.wrapper.ConfigObject

/**
  * Created by K.Misaki on 2017/06/01.
  *
  */
trait ReadableEntity {
  protected def load(obj: Object): Unit ={
    val config = ConfigObject.config
    val ext = config.getString("model-config.suffix")
    val prefix = config.getString("model-config.prefix")
    val empty = config.getString("standard-lib.empty")
    val fields = obj.getClass.getDeclaredFields
    // Propertiesオブジェクトを生成
    val properties = new Properties

    withFileReader(prefix + obj.getClass.getSimpleName + ext) { reader =>
      // ファイルを読み込む
      properties.load(reader)

      fields.foreach(field => {
        field.setAccessible(true)
        val cls = field.getType
        val pv = properties.getProperty(field.getName)
        val value: Any = cls match {
          case x if x == classOf[String] => pv
          case x if x == classOf[Byte] => pv.getBytes
          case x if x == classOf[Short] => pv.toShort
          case x if x == classOf[Int] => pv.toInt
          case x if x == classOf[Long] => pv.toLong
          case x if x == classOf[Boolean] => pv.toBoolean
          case _ => empty
        }
        if (value != null && !value.toString.isEmpty) field.set(obj, value) })
    }
  }

  private def withFileReader(file: String)(op: Reader => Unit) {
    val separator = File.separator
    var ioPath = new File(getPath).getCanonicalPath
    ioPath = if (ioPath.isEmpty || ioPath.endsWith(separator)) ioPath else ioPath + separator
    val reader = new FileReader(ioPath + file)
    try op(reader)
    finally reader.close()
  }

  def load
  protected def getPath: String
}
