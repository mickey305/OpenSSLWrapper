package com.mickey305.openssl.wrapper.scalafx.app

import java.awt.Desktop
import java.io._
import java.net.URL
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.PosixFilePermissions
import java.text.SimpleDateFormat
import java.util.Calendar
import javafx.{scene => jfxs}

import com.google.gson.Gson
import com.mickey305.openssl.wrapper.actor.utils.IdCache
import com.mickey305.openssl.wrapper.scalafx.controller.MainAppControllerInterface
import com.mickey305.openssl.wrapper.scalafx.entity.Config
import org.apache.commons.io.FileUtils

import scalafx.application.JFXApp
import scalafx.scene.{Parent, Scene}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object MainApp extends JFXApp {
  private val tmpRootDirPath = getSystemTmpDir +
    "JFXAppWork" + createUniqueTime("yyyyMMddHHmmssSSS") + File.separator
  private var home = System.getenv("HOME")
  home = if (home.endsWith(File.separator)) home else home + File.separator
  private val cachePath = home + ".opensslWrapperConfig" + File.separator

  createSucceeded(tmpRootDirPath) (_ => {
    initialize(tmpRootDirPath)
  })

  override def stopApp(): Unit = {
    super.stopApp()
    this.deleteFolder(tmpRootDirPath)
  }

  /**
    * initial task
    */
  def initialize(tmpPath: String): Unit = {
    val resource: Option[URL] = Option(getClass.getResource("MainApp.fxml"))
    if (resource.isEmpty) throw new IOException("Cannot load resource: MainApp.fxml")
    val loader = new FXMLLoader(resource.get, NoDependencyResolver)

    var config = new Config()
    val mngr = new ConfigManager(new Gson(), cachePath)
    if(!mngr.existConfig) mngr.save(config)
    config = mngr.load

    loader.load()

    val root = loader.getRoot[jfxs.Parent]
    val controller = loader.getController[MainAppControllerInterface]

    controller.setStage(stage)
    controller.setTmpPath(tmpPath)
    controller.setConfig(config)
    controller.setCacheDirectoryPath(cachePath)

    stage = new JFXApp.PrimaryStage() {
      title = "Openssl Wrapper App"
      scene = new Scene(new jfxs.Scene(root))
      resizable = false
    }
  }

  /**
    * replace scene
    * @param controller target window controller
    */
  def replaceScene(controller: Parent): Unit = {
    val scene: Option[jfxs.Scene] = Option(stage.scene.value)
    scene match {
      case Some(_) => stage.scene = new Scene(controller)
      case None    => stage.getScene.setRoot(controller)
    }
  }

  /**
    *
    * @param op
    */
  private def createSucceeded(targetFile: String)(op: Unit => Unit) {
    try {
      val created = createFolder(tmpRootDirPath)
      if (created) op()
    }
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
      Thread.sleep(internalMilliSec)
    }
    key
  }
}

class ConfigManager(gson: Gson, cacheDirPath: String) {
  private val path = cacheDirPath + "application_config.json"

  // config directory create
  private val cacheDir = new File(cacheDirPath)
  if (!cacheDir.exists()) cacheDir.mkdirs()

  def load: Config = {
    val jsonString = this.getChallengeJsonFile(path)
    gson.fromJson(jsonString, classOf[Config])
  }

  def save(config: Config): Unit = {
    val jsonString = gson.toJson(config)
    withPrintWriter(path) (writer => writer.println(jsonString))
  }

  def open(): Unit = Desktop.getDesktop.open(new File(path))

  def existConfig: Boolean = new File(path).exists()

  private def getChallengeJsonFile(filePath: String): String = {
    val outString = new StringBuilder
    withBufferedReader(filePath) (reader => {
      var line = ""
      while ({
        line = reader.readLine()
        line ne null
      }) outString.append(line)
    })
    outString.toString
  }

  private def withPrintWriter(filePath: String)(op: PrintWriter => Unit) {
    val writer = new PrintWriter(filePath)
    try op(writer)
    finally writer.close()
  }

  private def withBufferedReader(filePath: String)(op: BufferedReader => Unit) {
    val file = new File(filePath)

    if (!file.exists() || !file.isFile) return

    val reader = new FileReader(filePath)
    val bReader = new BufferedReader(reader)
    try op(bReader)
    finally {
      bReader.close()
    }
  }
}