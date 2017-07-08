package com.mickey.openssl.wrapper.actor.utils

import scala.collection.mutable

/**
  * Created by K.Misaki on 2017/06/18.
  *
  */
object IdCache {
  private var tmpDirNameSet: mutable.HashSet[String] = _

  /**
    * thread-base
    */
  def initTmpDirNameSet(): Unit = synchronized { tmpDirNameSet = new mutable.HashSet[String]() }

  def setTmpDirName(value: String): Boolean = {
    if (tmpDirNameSet == null) IdCache.initTmpDirNameSet()
    synchronized { return tmpDirNameSet.add(value) }
  }

  def tmpDirNameSetContains(value: String) = {
    if (tmpDirNameSet == null) IdCache.initTmpDirNameSet()
    tmpDirNameSet.contains(value)
  }
}
