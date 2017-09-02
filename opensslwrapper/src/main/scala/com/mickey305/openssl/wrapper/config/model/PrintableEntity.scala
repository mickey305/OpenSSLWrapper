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
