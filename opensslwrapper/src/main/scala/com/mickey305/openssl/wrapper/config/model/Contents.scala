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
