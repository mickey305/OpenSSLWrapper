package com.mickey305.openssl.wrapper

import java.io.File
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{Files, Paths}
import java.text.SimpleDateFormat
import java.util.Calendar

import com.mickey305.openssl.wrapper.actor.utils.IdCache
import com.mickey305.openssl.wrapper.config.model.{Contents, OpenSSL}
import com.mickey305.openssl.wrapper.exception.FilePathException
import com.mickey305.openssl.wrapper.exception.model.PathType.{Others, Package, PrivateKey, PublicKey}
import com.mickey305.util.cli.JournalManager
import com.mickey305.util.cli.receivers.ResultAccessibleReceiver
import com.mickey305.util.file.zip.ZipComponent
import org.apache.commons.io.FileUtils

/**
  * Created by K.Misaki on 2017/06/03.
  *
  */
class EncryptionFactory {
  val cliTskMngr = new EncryptionTaskManager
  val opensslCfg: OpenSSL = new OpenSSL
  var packageFilePath: String = _

  private val encryptionFileExt = ".encrypted"
  private val tmpRootDirPath = getSystemTmpDir +
    "work" + createUniqueTime("yyyyMMddHHmmssSSS") + File.separator
  private val tmpDirPath = tmpRootDirPath + this.getClass.getName + File.separator
  private val contentsFilePath = tmpDirPath + "contents" + encryptionFileExt
  private val contentsCfg = new Contents

  // default setting
  opensslCfg.shareKey = createUniqueTime("yyyyMMddHHmmssSSS") + ".share.key"
  opensslCfg.algorithm = "aes-256-cbc"
  opensslCfg.base64 = false
  cliTskMngr.receiver = new ResultAccessibleReceiver
  cliTskMngr.journalManager = new JournalManager

  /**
    * encryption flow
    * @param targetFilePath target encryption file path
    * @throws com.mickey305.openssl.wrapper.exception.FilePathException validation check
    */
  @throws(classOf[FilePathException])
  def pack(targetFilePath: String): Unit ={
    if (packageFilePath == null || packageFilePath.isEmpty)
      throw new FilePathException("error occurred.(package validation check)", Package)
    if (opensslCfg.publicKey == null)
      throw new FilePathException("error occurred.(publicKey validation check)", PublicKey)
    if (opensslCfg.publicKey.isEmpty)
      throw new FilePathException("error occurred.(publicKey validation check)", PublicKey)
    if (targetFilePath.isEmpty || !new File(targetFilePath).isFile)
      throw new FilePathException("error occurred.(inputFile validation check)", Others)
    cliTskMngr.cancelAll()

    val shareKeyPath = tmpDirPath + opensslCfg.shareKey
    val encShareKeyPath = shareKeyPath + encryptionFileExt
    val publicKeyPath = opensslCfg.publicKey
    cliTskMngr.subscribeShareKeyCreator(opensslCfg, shareKeyPath)
    cliTskMngr.subscribePKeyEncryption(opensslCfg, publicKeyPath, shareKeyPath, encShareKeyPath)
    cliTskMngr.subscribeShareKeyEncryption(opensslCfg, shareKeyPath, targetFilePath, contentsFilePath)

    tmpTask() { _ =>
      createFolder(tmpDirPath)
      // Encryption Task
      // execution
      cliTskMngr.invoke()
      // execution
      opensslCfg.version = cliTskMngr.opensslVersion()
      opensslCfg.path = tmpDirPath
      opensslCfg.save
      contentsCfg.path = tmpDirPath
      contentsCfg.fileName = targetFilePath.split(File.separator).last
      contentsCfg.save
      val shareKey = new File(shareKeyPath)
      val delete = shareKey.delete

      // ZIP
      if (delete) ZipComponent.compressDir(tmpDirPath, packageFilePath, null, null)
    }
  }

  /**
    * decryption flow
    * @param outDirPath target output base directory path
    * @param callbackVersionDiff version different callback
    * @throws com.mickey305.openssl.wrapper.exception.FilePathException validation Check
    */
  @throws(classOf[FilePathException])
  def unpack(outDirPath: String, callbackVersionDiff: Option[(String/*encrypted OpenSSL version*/, String/*decryption OpenSSL version*/) => Boolean] = None): Unit ={
    if (packageFilePath == null || packageFilePath.isEmpty)
      throw new FilePathException("error occurred.(package validation check)", Package)
    if (opensslCfg.privateKey == null)
      throw new FilePathException("error occurred.(privateKey validation check)", PrivateKey)
    if (opensslCfg.privateKey.isEmpty)
      throw new FilePathException("error occurred.(privateKey validation check)", PrivateKey)
    if (outDirPath.isEmpty)
      throw new FilePathException("error occurred.(outputDirectory validation check)", Others)
    val outDir = new File(outDirPath)
    if (!outDir.exists())
      outDir.mkdirs()
    cliTskMngr.cancelAll()
    val outPath = if (outDirPath.endsWith(File.separator)) outDirPath else outDirPath + File.separator

    tmpTask() { _ =>
      // UnZIP
      ZipComponent.decompress(packageFilePath, tmpRootDirPath, (_, _) => {
        val privateKeyPath = opensslCfg.privateKey
        // Decryption Task
        opensslCfg.path = tmpDirPath
        opensslCfg.load
        contentsCfg.path = tmpDirPath
        contentsCfg.load
        // execution
        val infoVersion = opensslCfg.version
        val thisVersion = cliTskMngr.opensslVersion()
        if (infoVersion == null || !infoVersion.equals(thisVersion))
          callbackVersionDiff match {
            case Some(status) => if(!status(infoVersion, thisVersion)) return }
        val shareKeyPath = tmpDirPath + opensslCfg.shareKey
        val encShareKeyPath = shareKeyPath + encryptionFileExt
        cliTskMngr.subscribePKeyDecryption(opensslCfg, privateKeyPath, encShareKeyPath, shareKeyPath)
        cliTskMngr.subscribeShareKeyDecryption(opensslCfg, shareKeyPath, contentsFilePath, outPath + contentsCfg.fileName)
        // execution
        cliTskMngr.invoke()
      }, null)
    }
  }

  /**
    *
    * @param op
    */
  private def tmpTask()(op: String => Unit) {
    try {
      val created = createFolder(tmpRootDirPath)
      if (created) op(tmpRootDirPath)
    } finally {
      deleteFolder(tmpRootDirPath)
    }
  }

  /**
    *
    * @param folderName
    * @param permission
    * @return
    */
  private def createFolder(folderName: String, permission: String = "rwx------"): Boolean = {
    val folder = new File(folderName)
    if (!folder.exists) {
      folder.getParentFile.mkdirs()
      Files.createDirectory(
        Paths.get(folder.getCanonicalPath),
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(permission)))
      return folder.exists
    }
    false
  }

  /**
    *
    * @param folderName
    */
  private def deleteFolder(folderName: String): Unit = {
    val folder = new File(folderName)
    if (folder.isDirectory) FileUtils.deleteDirectory(folder)
  }

  /**
    *
    * @return
    */
  private def getSystemTmpDir = {
    val tmpDir = System.getProperty("java.io.tmpdir")
    if (tmpDir.endsWith(File.separator)) tmpDir else tmpDir + File.separator
  }

  /**
    *
    * @param format
    * @return
    */
  private def createUniqueTime(format: String): String = {
    val tryCount = 1000
    val internalMilliSec = 1
    val sdf = new SimpleDateFormat(format)
    var key: String = null
    0.until(tryCount).foreach { i =>
      val cal = Calendar.getInstance
      key = sdf.format(cal.getTime)
      if (IdCache.setTmpDirName(key)) return key
//      println(">>> CREATE TMP DIR RETRY [" + (i + 1) + "]: conflict directory name - work" + key)
      Thread.sleep(internalMilliSec)
    }
    key
  }
}
