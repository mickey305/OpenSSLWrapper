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
package com.mickey305.openssl.wrapper.scalafx.entity

import java.sql.Timestamp

import scalafx.beans.property.{IntegerProperty, LongProperty, ObjectProperty, StringProperty}

/**
  * Created by K.Misaki on 2017/07/31.
  *
  */
class Log(id_ : Long, pid_ : Int, cmd_ : String, start_ : Timestamp, end_ : Timestamp) {
  val id = new LongProperty(this, "id", id_)
  val pid = new IntegerProperty(this, "pid", pid_)
  val cmd = new StringProperty(this, "cmd", cmd_)
  val start = new ObjectProperty[Timestamp](this, "start", start_)
  val end = new ObjectProperty[Timestamp](this, "end", end_)

  def setId(id: Long): Unit = this.id.set(id)

  def getId(): Long = this.id.get

  def setPid(pid: Int): Unit = this.pid.set(pid)

  def getPid(): Int = this.pid.get

  def setCmd(cmd: String): Unit = this.cmd.set(cmd)

  def getCmd(): String = this.cmd.get

  def setStart(start: Timestamp): Unit = this.start.set(start)

  def getStart(): Timestamp = this.start.get

  def setEnd(end: Timestamp): Unit = this.end.set(end)

  def getEnd(): Timestamp = this.end.get
}
