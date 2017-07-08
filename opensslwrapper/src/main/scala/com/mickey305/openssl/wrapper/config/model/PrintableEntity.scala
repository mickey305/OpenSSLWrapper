package com.mickey305.openssl.wrapper.config.model

import java.util.function.UnaryOperator

import com.mickey305.openssl.wrapper.ConfigObject

/**
  * Created by K.Misaki on 2017/06/01.
  *
  */
trait PrintableEntity {
  protected def console(obj: Object, sig: Int): Unit ={
    val config = ConfigObject.config
    val empty = config.getString("standard-lib.empty")
    val fields = obj.getClass.getDeclaredFields
    val wrpDq: UnaryOperator[Any] = (v: Any) => "\"" + v + "\""

    plot("{", sig)
    fields.zipWithIndex.foreach( target => {
      val field = target._1
      val index = target._2
      field.setAccessible(true)
      val key = field.getName
      val value = if (field != null && field.get(obj) != null) field.get(obj).toString else empty
      plot(wrpDq.apply(key) + ":" + wrpDq.apply(value) + (if (index != fields.length -1) "," else empty), sig) })
    plot("}", 4)
  }

  private def plot(msg: Any, sig: Int): Unit ={
    if (sig == 0) print(msg) else println(msg)
  }

  def console(sig: Int = 0)
  def console
}
