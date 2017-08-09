package com.mickey305.openssl.wrapper.scalafx.extension

/**
  * Created by K.Misaki on 2017/08/10.
  *
  */
class StringBuilderExtension(builder: StringBuilder) {
  /**
    * append strings
    * @param strs target strings
    * @return [[scala.StringBuilder]]
    */
  def append(strs: String*): StringBuilder = {
    strs.foreach(builder.append)
    builder
  }

  /**
    * append return-code
    * @return [[scala.StringBuilder]]
    */
  def appendln: StringBuilder = builder.append(System.lineSeparator)

  /**
    * append string and return-code.
    * @param str target string
    * @return [[scala.StringBuilder]]
    */
  def appendln(str: String): StringBuilder = {
    builder.append(str)
    this.appendln
  }

  /**
    * append strings and return-code.
    * @param strs target strings
    * @return [[scala.StringBuilder]]
    */
  def appendln(strs: String*): StringBuilder = {
    this.append(strs: _*)
    this.appendln
  }

  /**
    * append lines of string.
    * @param strs target strings
    * @return [[scala.StringBuilder]]
    */
  def appendlnAll(strs: String*): StringBuilder = {
    strs.foreach(this.appendln)
    builder
  }
}
