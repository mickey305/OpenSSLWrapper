package com.mickey305.openssl.wrapper.scalafx.extension

import scala.collection.mutable

/**
  * Created by K.Misaki on 2017/08/10.
  *
  */
class StringBuilderExtension(builder: mutable.StringBuilder) {
  /**
    * append strings
    * @param strs target strings
    * @return [[scala.collection.mutable.StringBuilder]]
    */
  def appends(strs: String*): mutable.StringBuilder = {
    strs.foreach(builder.append)
    builder
  }

  /**
    * append return-code
    * @return [[scala.collection.mutable.StringBuilder]]
    */
  def appendln: mutable.StringBuilder = builder.append(System.lineSeparator)

  /**
    * append string and return-code.
    * @param str target string
    * @return [[scala.collection.mutable.StringBuilder]]
    */
  def appendln(str: String): mutable.StringBuilder = {
    builder.append(str)
    this.appendln
  }

  /**
    * append strings and return-code.
    * @param strs target strings
    * @return [[scala.collection.mutable.StringBuilder]]
    */
  def appendln(strs: String*): mutable.StringBuilder = {
    this.appends(strs: _*)
    this.appendln
  }

  /**
    * append lines of string.
    * @param strs target strings
    * @return [[scala.collection.mutable.StringBuilder]]
    */
  def appendlnAll(strs: String*): mutable.StringBuilder = {
    strs.foreach(this.appendln)
    builder
  }
}
