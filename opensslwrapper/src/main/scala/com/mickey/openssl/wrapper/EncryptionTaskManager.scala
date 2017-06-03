package com.mickey.openssl.wrapper

import java.text.SimpleDateFormat
import java.util.Calendar

import com.mickey.openssl.wrapper.config.model.OpenSSL
import com.mickey305.util.cli.commands.OpenSSLCommand
import com.mickey305.util.cli.invokers.TermInvoker
import com.mickey305.util.cli.model.TerminalCommandJournal
import com.mickey305.util.cli.receivers.ResultAccessibleReceiver
import com.mickey305.util.cli.{CliReceiver, JournalManager, TerminalCommand}

/**
  * Created by K.Misaki on 2017/06/03.
  *
  */
class EncryptionTaskManager {
  var cmdStatus = false
  var opensslConfig: OpenSSL = new OpenSSL
  var receiver: CliReceiver = _
  var journalManager: JournalManager = _

  private val openssl = OpenSSLCommand.create(status => cmdStatus = status)
  private val leftHyphen = (v: String) => if (v.startsWith("-")) v else "-" + v
  private var invoker = new TermInvoker[TerminalCommand]

  // default setting
  opensslConfig.shareKey = createUniqueTime("yyyyMMddHHmmssSSS") + ".share.key"
  opensslConfig.algorithm = "aes-256-cbc"
  opensslConfig.base64 = false

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

  def subscribeShareKeyCreator(kPath: String): Unit = {
    // openssl rand 32 -out {SHARE_KEY_FILE} [-base64]
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.randValue, 32.toString)
      .option(leftHyphen.apply("out"), kPath)
    if (opensslConfig.base64)
      cmd.option(leftHyphen.apply(opensslConfig.base64Value))
    invoker.add(cmd)
  }

  def subscribeShareKeyEncryption(kPath: String, inPath: String, outPath: String): Unit = {
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

  def subscribeShareKeyDecryption(kPath: String, inPath: String, outPath: String): Unit = {
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

  def subscribePrivateKeyCreator(privateKeyPath: String): Unit = {
    // openssl genrsa 2048 > {PRIVATE_PEM_FILE}
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.genRsaValue, 2048.toString)
      .option(">", privateKeyPath)
    invoker.add(cmd)
  }

  def subscribePublicKeyCreator(privateKeyPath: String, publicKeyPath: String): Unit = {
    // openssl rsa -in {PRIVATE_PEM_FILE} -pubout -out {PUBLIC_PEM_FILE}
    if (!cmdStatus) return
    val cmd = openssl.clone.receiver(receiver)
      .option(opensslConfig.rsaValue)
      .option(leftHyphen.apply("in"), privateKeyPath)
      .option(leftHyphen.apply("pubout")).option(leftHyphen.apply("out"), publicKeyPath)
    invoker.add(cmd)
  }

  def subscribePKeyEncryption(kPath: String, inPath: String, outPath: String): Unit = {
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

  def subscribePKeyDecryption(kPath: String, inPath: String, outPath: String): Unit = {
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
    val receiver = new ResultAccessibleReceiver
    val cmd = openssl.clone().receiver(receiver).option("version")
    cmd.execute()
    // add journal
    if(journalManager != null) journalManager.createAndAddJournal(cmd.getPid, receiver, cmd.getTimestampMap)
    val cache = receiver.getResultCacheSet.stream.findFirst.get
    cache.getResult
  }

  private def createUniqueTime(format: String): String = {
    val cal = Calendar.getInstance
    val sdf = new SimpleDateFormat(format)
    sdf.format(cal.getTime)
  }
}
