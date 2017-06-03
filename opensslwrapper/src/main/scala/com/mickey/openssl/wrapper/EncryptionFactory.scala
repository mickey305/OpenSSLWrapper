package com.mickey.openssl.wrapper

import java.io.File

import com.mickey.openssl.wrapper.config.model.Contents
import com.mickey305.util.cli.JournalManager
import com.mickey305.util.cli.receivers.ResultAccessibleReceiver
import com.mickey305.util.file.zip.ZipComponent
import org.apache.commons.io.FileUtils

/**
  * Created by K.Misaki on 2017/06/03.
  *
  */
class EncryptionFactory {
  val encryptionTaskManager = new EncryptionTaskManager
  var packageFilePath: String = _

  private val encryptionFileExt = ".encrypted"
  private val tmpDirPath = System.getProperty("java.io.tmpdir") + this.getClass.getName + File.separator
  private val contentsFilePath = tmpDirPath + "contents" + encryptionFileExt
  private val contentsConfig = new Contents

  encryptionTaskManager.receiver = new ResultAccessibleReceiver
  encryptionTaskManager.journalManager = new JournalManager

  /**
    *
    * @param targetFilePath
    */
  def pack(targetFilePath: String): Unit ={
    if (packageFilePath == null || packageFilePath.isEmpty) return
    if (encryptionTaskManager.opensslConfig.publicKey == null) return
    if (encryptionTaskManager.opensslConfig.publicKey.isEmpty) return
    encryptionTaskManager.cancelAll()

    val shareKeyPath = tmpDirPath + encryptionTaskManager.opensslConfig.shareKey
    val encShareKeyPath = shareKeyPath + encryptionFileExt
    val publicKeyPath = encryptionTaskManager.opensslConfig.publicKey
    encryptionTaskManager.subscribeShareKeyCreator(shareKeyPath)
    encryptionTaskManager.subscribePKeyEncryption(publicKeyPath, shareKeyPath, encShareKeyPath)
    encryptionTaskManager.subscribeShareKeyEncryption(shareKeyPath, targetFilePath, contentsFilePath)

    tmpTask(createFolder(tmpDirPath)) { _ =>
      // Encryption Task
      // execution
      encryptionTaskManager.invoke()
      // execution
      encryptionTaskManager.opensslConfig.version = encryptionTaskManager.opensslVersion()
      encryptionTaskManager.opensslConfig.path = tmpDirPath
      encryptionTaskManager.opensslConfig.save
      contentsConfig.path = tmpDirPath
      contentsConfig.fileName = targetFilePath.split(File.separator).last
      contentsConfig.save
      val shareKey = new File(shareKeyPath)
      val delete = shareKey.delete

      // ZIP
      if (delete) ZipComponent.compressDir(tmpDirPath, packageFilePath, null, null)
    }
  }

  /**
    *
    * @param outDirPath
    */
  def unpack(outDirPath: String): Unit ={
    if (packageFilePath == null || packageFilePath.isEmpty) return
    if (encryptionTaskManager.opensslConfig.privateKey == null) return
    if (encryptionTaskManager.opensslConfig.privateKey.isEmpty) return
    encryptionTaskManager.cancelAll()
    val outPath = if (outDirPath.endsWith(File.separator)) outDirPath else outDirPath + File.separator

    // UnZIP
    ZipComponent.decompress(packageFilePath, new File(tmpDirPath).getParent, (_, _) => {
      tmpTask() { _ =>
        // Decryption Task
        encryptionTaskManager.opensslConfig.path = tmpDirPath
        encryptionTaskManager.opensslConfig.load
        contentsConfig.path = tmpDirPath
        contentsConfig.load
        // execution
        if (!encryptionTaskManager.opensslConfig.version.equals(encryptionTaskManager.opensslVersion())) return
        val shareKeyPath = tmpDirPath + encryptionTaskManager.opensslConfig.shareKey
        val encShareKeyPath = shareKeyPath + encryptionFileExt
        val privateKeyPath = encryptionTaskManager.opensslConfig.privateKey
        encryptionTaskManager.subscribePKeyDecryption(privateKeyPath, encShareKeyPath, shareKeyPath)
        encryptionTaskManager.subscribeShareKeyDecryption(shareKeyPath, contentsFilePath, outPath + contentsConfig.fileName)
        // execution
        encryptionTaskManager.invoke() }
    }, null)
  }

  /**
    *
    * @param status
    * @param op
    */
  private def tmpTask(status: Boolean = true)(op: Unit => Unit) {
    try if (status) op()
    finally deleteFolder(tmpDirPath)
  }

  /**
    *
    * @param folderName
    * @return
    */
  private def createFolder(folderName: String): Boolean = {
    val folder = new File(folderName)
    if (!folder.exists())
      return folder.mkdirs()
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
}
