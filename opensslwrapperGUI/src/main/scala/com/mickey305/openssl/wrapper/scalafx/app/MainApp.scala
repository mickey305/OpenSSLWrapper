package com.mickey305.openssl.wrapper.scalafx.app

import java.awt.Desktop
import java.io.{File, IOException}
import java.net.URL
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.PosixFilePermissions
import java.text.SimpleDateFormat
import java.util.Calendar
import javafx.{scene => jfxs}

import com.mickey305.openssl.wrapper.actor.utils.IdCache
import com.mickey305.openssl.wrapper.scalafx.controller.MainAppControllerInterface
import org.apache.commons.io.FileUtils

import scalafx.application.JFXApp
import scalafx.scene.{Parent, Scene}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object MainApp extends JFXApp {
  private val tmpRootDirPath = getSystemTmpDir +
    "JFXAppWork" + createUniqueTime("yyyyMMddHHmmssSSS") + File.separator

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

    loader.load()

    val root = loader.getRoot[jfxs.Parent]
    val controller = loader.getController[MainAppControllerInterface]

    controller.setStage(stage)
    controller.setTmpPath(tmpPath)

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