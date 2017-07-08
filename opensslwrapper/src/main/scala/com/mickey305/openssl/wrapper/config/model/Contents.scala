package com.mickey305.openssl.wrapper.config.model

/**
  * Created by K.Misaki on 2017/06/03.
  *
  */
class Contents extends WritableEntity with ReadableEntity with PrintableEntity {
  var path = ""
  var fileName = ""
  var freeSpace = ""

  override def save: Unit = super.save(this)
  override def load: Unit = super.load(this)
  override protected def getPath: String = this.path
  override def console(sig: Int): Unit = super.console(this, sig)
  override def console: Unit = this.console()
}
