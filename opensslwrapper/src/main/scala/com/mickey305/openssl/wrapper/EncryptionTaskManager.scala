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
package com.mickey305.openssl.wrapper

import com.mickey305.openssl.wrapper.config.model.OpenSSL
import com.mickey305.openssl.wrapper.model.OptionKey
import com.mickey305.util.cli.commands.OpenSSLCommand
import com.mickey305.util.cli.invokers.TermInvoker
import com.mickey305.util.cli.model.TerminalCommandJournal
import com.mickey305.util.cli.receivers.ResultAccessibleReceiver
import com.mickey305.util.cli.{CliReceiver, JournalManager, TerminalCommand}

/**
  * Created by K.Misaki on 2017/06/03.
  *
  */
class EncryptionTaskManager(callbackCmdSearch: Option[Unit => OpenSSLCommand] = None) {
  var cmdStatus = false
  var receiver: CliReceiver = _
  var journalManager: JournalManager = _

  private val leftHyphen = (v: String) => if (v.startsWith("-")) v else "-" + v
  private var invoker = new TermInvoker[TerminalCommand]
  private var openssl: OpenSSLCommand = OpenSSLCommand.create(status => cmdStatus = status)
  if (!cmdStatus) {
    callbackCmdSearch match {
      case Some(cmd) => {
        openssl = cmd()
        cmdStatus = openssl != null}}
  }

  def this() = this(None)

  def invoke(): Unit = {
    invoker.setJournalManager(journalManager)
    invoker.execute()
  }

  def journal(): List[TerminalCommandJournal] = {
    import scala.collection.JavaConverters._
    var list = List[TerminalCommandJournal]()
    if (invoker.getJournalManager != null && invoker.getJournalManager.getJournalList != null)
      list = invoker.getJournalManager.getJournalList.asScala.toList
    list
  }

  def cancelAll(): Unit = {
    invoker = new TermInvoker[TerminalCommand]
  }

  def subscribeShareKeyCreator(opensslConfig: OpenSSL, kPath: String, options: Map[OptionKey, String] = Map(
    // default setting
    OptionKey.ShareKeyLength -> 32.toString
  )): Unit = {
    // openssl rand 32 -out {SHARE_KEY_FILE} [-base64]
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.randValue, options(OptionKey.ShareKeyLength))
      .option(leftHyphen.apply("out"), kPath)
    if (opensslConfig.base64)
      cmd.option(leftHyphen.apply(opensslConfig.base64Value))
    invoker.add(cmd)
  }

  def subscribeShareKeyEncryption(opensslConfig: OpenSSL, kPath: String, inPath: String, outPath: String): Unit = {
    // openssl {ALGORITHM} -e -in {ORIGINAL_FILE} -out {ENCRYPTION_FILE} -pass file:{SHARE_KEY_FILE} [-base64]
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.algorithm, leftHyphen.apply("e"))
      .option(leftHyphen.apply("in"), inPath)
      .option(leftHyphen.apply("out"), outPath)
      .option(leftHyphen.apply("pass"), "file:" + kPath)
    if (opensslConfig.base64)
      cmd.option(leftHyphen.apply(opensslConfig.base64Value))
    invoker.add(cmd)
  }

  def subscribeShareKeyDecryption(opensslConfig: OpenSSL, kPath: String, inPath: String, outPath: String): Unit = {
    // openssl {ALGORITHM} -d -in {ENCRYPTION_FILE} -out {ORIGINAL_FILE} -pass file:{SHARE_KEY_FILE} [-base64]
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.algorithm, leftHyphen.apply("d"))
      .option(leftHyphen.apply("in"), inPath)
      .option(leftHyphen.apply("out"), outPath)
      .option(leftHyphen.apply("pass"), "file:" + kPath)
    if (opensslConfig.base64)
      cmd.option(leftHyphen.apply(opensslConfig.base64Value))
    invoker.add(cmd)
  }

  def subscribePrivateKeyCreator(opensslConfig: OpenSSL, privateKeyPath: String, options: Map[OptionKey, String] = Map(
    // default setting
    OptionKey.PrivateKeyLength -> 2048.toString
  )): Unit = {
    // openssl genrsa 2048 > {PRIVATE_PEM_FILE}
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.genRsaValue, options(OptionKey.PrivateKeyLength))
      .option(">", privateKeyPath)
    invoker.add(cmd)
  }

  def subscribePublicKeyCreator(opensslConfig: OpenSSL, privateKeyPath: String, publicKeyPath: String): Unit = {
    // openssl rsa -in {PRIVATE_PEM_FILE} -pubout -out {PUBLIC_PEM_FILE}
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.rsaValue)
      .option(leftHyphen.apply("in"), privateKeyPath)
      .option(leftHyphen.apply("pubout")).option(leftHyphen.apply("out"), publicKeyPath)
    invoker.add(cmd)
  }

  def subscribePKeyEncryption(opensslConfig: OpenSSL, kPath: String, inPath: String, outPath: String): Unit = {
    // openssl rsautl -encrypt -pubin -inkey {PUBLIC_PEM_FILE} -in {ORIGINAL_FILE} -out {ENCRYPTION_FILE}
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.rsaUtilValue, leftHyphen.apply("encrypt"))
      .option(leftHyphen.apply("pubin"))
      .option(leftHyphen.apply("inkey"), kPath)
      .option(leftHyphen.apply("in"), inPath)
      .option(leftHyphen.apply("out"), outPath)
    invoker.add(cmd)
  }

  def subscribePKeyDecryption(opensslConfig: OpenSSL, kPath: String, inPath: String, outPath: String): Unit = {
    // openssl rsautl -decrypt -inkey {PRIVATE_PEM_FILE} -in {ENCRYPTION_FILE} -out {ORIGINAL_FILE}
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.rsaUtilValue, leftHyphen.apply("decrypt"))
      .option(leftHyphen.apply("inkey"), kPath)
      .option(leftHyphen.apply("in"), inPath)
      .option(leftHyphen.apply("out"), outPath)
    invoker.add(cmd)
  }

  def opensslVersion(): String ={
    if (!cmdStatus) return null
    val receiver = new ResultAccessibleReceiver
    val cmd = openssl.clone.receiver(receiver).option("version")
    cmd.execute()
    // add journal
    if (journalManager != null) journalManager.createAndAddJournal(cmd.getPid, receiver, cmd.getTimestampMap)
    val optCache = receiver.getResultCacheSet.stream.findFirst
    if (optCache.isPresent) optCache.get.getResult else null
  }
}
