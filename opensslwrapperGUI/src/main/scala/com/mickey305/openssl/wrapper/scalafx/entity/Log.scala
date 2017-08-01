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
