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
package com.mickey305.openssl.wrapper.scalafx.controller

import java.awt.Desktop
import java.io.File
import java.sql.Timestamp
import javafx.collections.ObservableList

import com.google.gson.Gson
import com.mickey305.openssl.wrapper.EncryptionFactory
import com.mickey305.openssl.wrapper.exception.FilePathException
import com.mickey305.openssl.wrapper.scalafx.app.ConfigManager
import com.mickey305.openssl.wrapper.scalafx.entity.{Config, Log}
import com.mickey305.openssl.wrapper.scalafx.extension.{StringBuilderExtension => StrBuilderExt}
import com.mickey305.util.cli.model.Benchmark

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.stage.{DirectoryChooser, FileChooser, Stage}
import scalafxml.core.macros.sfxml

trait MainAppControllerInterface {
  def setStage(stage: Stage): Unit
  def setTmpPath(path: String): Unit
  def setConfig(config: Config): Unit
  def setCacheDirectoryPath(cacheDir: String): Unit
  def loadDefaultSetting: Unit
}

@sfxml
class MainAppController(val pubPathTf: TextField, val browsePubBtn: Button,
                        val prvPathTf: TextField, val browsePrvBtn: Button,
                        val pkgPathTf: TextField, val browsePkgBtn: Button,
                        val inPathTf: TextField, val browseInBtn: Button,
                        val outPathTf: TextField, val browseOutBtn: Button,
                        val encLbl: Label, val encTgl: CheckBox,
                        val table: TableView[Log],
                        val idCol: TableColumn[Log, Long],
                        val pidCol: TableColumn[Log, Int],
                        val cmdCol: TableColumn[Log, String],
                        val startCol: TableColumn[Log, Timestamp],
                        val endCol: TableColumn[Log, Timestamp],
                        val logExportBtn: Button,
                        val tmpDirLink: Hyperlink,
                        val configFileLink: Hyperlink,
                        val settingLoadBtn: Button,
                        val settingStoreBtn: Button,
                        val runBtn: Button)
  extends MainAppControllerInterface {

  // extension of StringBuilder class
  private implicit def extStrBuilder(builder: StringBuilder): StrBuilderExt = new StrBuilderExt(builder)

  private var config: Config = _
  private var cacheDir: String = _
  private var tmpDirPath: String = _
  private var stage: Stage = _

  toggleContents(encTgl.selected.value)

  //===--- PublicKey Browse Button Event --------------------------------------------------------------------------===//
  browsePubBtn.onMouseClicked = (e: MouseEvent) => {
    val fileChooser = new FileChooser()
    fileChooser.extensionFilters.addAll(
      new FileChooser.ExtensionFilter("Public Key File", Seq("*.pem"))
    )
    val file = fileChooser.showOpenDialog(stage)

    setTextTo(pubPathTf, file)
  }

  //===--- PrivateKey Browse Button Event -------------------------------------------------------------------------===//
  browsePrvBtn.onMouseClicked = (e: MouseEvent) => {
    val fileChooser = new FileChooser()
    fileChooser.extensionFilters.addAll(
      new FileChooser.ExtensionFilter("Private Key File", Seq("*.pem"))
    )
    val file = fileChooser.showOpenDialog(stage)

    setTextTo(prvPathTf, file)
  }

  //===--- PackageFile Browse Button Event ------------------------------------------------------------------------===//
  browsePkgBtn.onMouseClicked = (e: MouseEvent) => {
    var file: File = null
    if (!encTgl.selected.value) {
      val fileChooser = new FileChooser()
      fileChooser.extensionFilters.addAll(
        new FileChooser.ExtensionFilter("Encbox File", Seq("*.encbox"))
      )
      file = fileChooser.showOpenDialog(stage)
    } else {
      val dirChooser = new DirectoryChooser()
      file = dirChooser.showDialog(stage)
      if (inPathTf.getText.isEmpty && file != null) {
        file = new File(file.getCanonicalPath + File.separator + "Target.encbox")
      } else if (file != null) {
        val fary = inPathTf.getText.split(if (File.separator.equals("\\")) "\\\\" else File.separator, -1)
        val fnameAll = fary(fary.size - 1)
        val fname = fnameAll.substring(0, fnameAll.lastIndexOf("."))
        file = new File(file.getCanonicalPath + File.separator + (
          if (fname.isEmpty) "Target.encbox" else fname + ".encbox"))
      }
    }

    // package path text field <--- chosen file canonical path
    setTextTo(pkgPathTf, file)
  }

  //===--- InputFile Browse Button Event --------------------------------------------------------------------------===//
  browseInBtn.onMouseClicked = (e: MouseEvent) => {
    val fileChooser = new FileChooser()
    fileChooser.extensionFilters.addAll(
      new FileChooser.ExtensionFilter("Input File", Seq("*.*"))
    )
    val file = fileChooser.showOpenDialog(stage)

    setTextTo(inPathTf, file)
  }

  //===--- OutputDirectory Browse Button Event --------------------------------------------------------------------===//
  browseOutBtn.onMouseClicked = (e: MouseEvent) => {
    val dirChooser = new DirectoryChooser()
    val file = dirChooser.showDialog(stage)

    if (file != null && file.isDirectory) setTextTo(outPathTf, file)
  }

  //===--- Encryption Button Event --------------------------------------------------------------------------------===//
  encTgl.onMouseClicked = (e: MouseEvent) => {
    toggleContents(encTgl.selected.value)
  }

  //===--- TmpDir Hyperlink Event ---------------------------------------------------------------------------------===//
  tmpDirLink.onMouseClicked = (e: MouseEvent) => {
    Desktop.getDesktop.open(new File(tmpDirPath))
  }

  //===--- ConfigFile Hyperlink Event -----------------------------------------------------------------------------===//
  configFileLink.onMouseClicked = (e: MouseEvent) => {
    val mngr = new ConfigManager(new Gson(), cacheDir)
    mngr.open()
  }

  //===--- Run Button Event ---------------------------------------------------------------------------------------===//
  runBtn.onMouseClicked = (e: MouseEvent) => {
    // execute encryption(or decryption) task
    val prvKey = prvPathTf.getText
    val pubKey = pubPathTf.getText
    val pkgPath = pkgPathTf.getText
    val inPath = inPathTf.getText
    val outPath = outPathTf.getText
    val opt: Option[(String, String) => Boolean] = Option((encV, decV) => {
      val lines = new StringBuilder
      lines.appendln("Encryption OpenSSL Version [", encV, "]")
      lines.appends("This PC's OpenSSL Version [", decV, "]")
      val textArea = new TextArea(lines.toString)
      val alert = new Alert(
        AlertType.Warning,
        "This PC OpenSSL Version is different from encrypted OpenSSL version",
        ButtonType.Cancel, ButtonType.OK)
      alert.setTitle("Version Warning Information!")
      alert.getDialogPane.setExpandableContent(textArea)
      alert.showAndWait() match {
        case Some(value) => value == ButtonType.OK
        case None => false
      }
    })

    val ef = new EncryptionFactory
    ef.opensslCfg.publicKey = pubKey
    ef.opensslCfg.privateKey = prvKey
    ef.packageFilePath = pkgPath

    val lines = new StringBuilder
    if (encTgl.selected.value) {
      lines.appendln("+ task mode: Encryption")
      lines.appendln("+ public key file path: ", ?(pubKey))
      lines.appendln("+ input file(encryption target): ", ?(inPath))
      lines.appendln("+ package file path: ", ?(pkgPath))
    } else {
      lines.appendln("+ task mode: Decryption")
      lines.appendln("+ private key file path: ", ?(prvKey))
      lines.appendln("+ package file path: ", ?(pkgPath))
      lines.appendln("+ output directory path: ", ?(outPath))
    }

    val confAlert = new Alert(AlertType.Confirmation, lines.toString(), ButtonType.Cancel, ButtonType.OK)
    confAlert.setTitle("Task Execution Confirmation!")
    val status = confAlert.showAndWait() match {
      case Some(value) => value == ButtonType.OK
      case None => false
    }

    try {
      // post task to background libraries
      if (encTgl.selected.value && status) {
        ef.pack(inPath)
      }
      if (!encTgl.selected.value && status) {
        ef.unpack(outPath, opt)
      }
      // create log list
      if (status) {
        val items: ObservableList[Log] = table.getItems
        val idOffset = if (!items.isEmpty) items.maxBy(_.getId()).getId() + 1 else 0
        ef.cliTskMngr.journal().foreach(journal => {
          items.add(
            new Log(
              journal.getId + idOffset,
              if (journal.getPid < 0) "N/A" else String.valueOf(journal.getPid),
              journal.getExecutionSentence,
              journal.getTimestampMaps.get(Benchmark.START),
              journal.getTimestampMaps.get(Benchmark.END)))
        })
        //        items.forEach(e => println(e.cmd.toString))
      }
    } catch {
      case e: FilePathException => {
        val dialog = errorDialogCreator(e)
        dialog.showAndWait()
      }
      case e: Exception => {
        e.printStackTrace() // stack trace log output
        val dialog = errorDialogCreator(e)
        dialog.showAndWait()
      }
    }
  }

  //===------------------------------------------------------------------------------------------------------------===//
  def handleExportExcel: Unit = {
    val targetExcelFilePath = tmpDirPath + this.getClass.getName + "_LogList.xlsx"
    val excelExporter = new ExcelExporter[Log](targetExcelFilePath)
    excelExporter.execute(table.getItems, log => Seq(
      log.getId().toString,
      log.getPid().toString,
      log.getCmd().toString,
      log.getStart().toString,
      log.getEnd().toString))
    Desktop.getDesktop.open(new File(targetExcelFilePath))
  }

  def handleLogClear: Unit = table.getItems.clear()

  def handleDefaultSettingLoad: Unit = {
    settingLoadBtn.disable = true
    settingStoreBtn.disable = true
    if (!config.getPublicKeyPath.isEmpty) setTextTo(pubPathTf, new File(config.getPublicKeyPath))
    if (!config.getPublicKeyPath.isEmpty) pubPathTf.style = "-fx-text-inner-color: green;"
    if (!config.getPrivateKeyPath.isEmpty) setTextTo(prvPathTf, new File(config.getPrivateKeyPath))
    if (!config.getPrivateKeyPath.isEmpty) prvPathTf.style = "-fx-text-inner-color: green;"
    encTgl.selected = config.isExecutionEncryptionMode
    toggleContents(encTgl.selected.value)
    encLbl.style = "-fx-text-fill: green;"
    val th = new Thread {
      setDaemon(true)
      override def run = {
        Thread.sleep(500)
        Platform.runLater {
          if (!config.getPublicKeyPath.isEmpty) pubPathTf.style = "-fx-text-inner-color: black;"
          if (!config.getPrivateKeyPath.isEmpty) prvPathTf.style = "-fx-text-inner-color: black;"
          encLbl.style = "-fx-text-fill: black;"
          settingLoadBtn.disable = false
          settingStoreBtn.disable = false
        }
      }
    }
    th.start()
  }

  def handleDefaultSettingStore: Unit = {
    config.setPublicKeyPath(pubPathTf.getText)
    config.setPrivateKeyPath(prvPathTf.getText)
    config.setExecutionEncryptionMode(encTgl.selected.value)
    val mngr = new ConfigManager(new Gson(), cacheDir)
    mngr.save(config)
  }

  private def filterString(target: String): String = if (target == null || target.isEmpty) "[NULL]" else target

  // Todo: escape method logic creation
  private def escaping(target: String): String = {
    target
  }

  private def ?(target: String): String = escaping(filterString(target))

  private def toggleContents(isEnc: Boolean): Unit = {
    if (isEnc) {
      encLbl.setText("Encryption")
      outPathTf.disable = true
      prvPathTf.disable = true
      inPathTf.disable = false
      pubPathTf.disable = false
      pkgPathTf.disable = false
      browseOutBtn.disable = true
      browsePrvBtn.disable = true
      browseInBtn.disable = false
      browsePubBtn.disable = false
      browsePkgBtn.disable = false
    } else {
      encLbl.setText("Decryption")
      outPathTf.disable = false
      prvPathTf.disable = false
      inPathTf.disable = true
      pubPathTf.disable = true
      pkgPathTf.disable = false
      browseOutBtn.disable = false
      browsePrvBtn.disable = false
      browseInBtn.disable = true
      browsePubBtn.disable = true
      browsePkgBtn.disable = false
    }
  }

  private def setTextTo(target: TextField, file: File): Unit = if (file != null) target.setText(file.getCanonicalPath)

  private def errorDialogCreator(th: Throwable): Alert = {
    var stackTraceMsg = th.toString
    th.getStackTrace.foreach(elm => stackTraceMsg += System.lineSeparator() + elm.toString)
    val alert = new Alert(AlertType.Error)
    val textArea = new TextArea(stackTraceMsg)

    alert.setTitle("Error Occurred!")
    alert.setContentText("An exception was thrown in the application")
    alert.getDialogPane.setExpandableContent(textArea)
    alert
  }

  override def setStage(stage: Stage): Unit = this.stage = stage

  override def setTmpPath(path: String): Unit = this.tmpDirPath = path

  override def setConfig(config: Config): Unit = this.config = config

  override def setCacheDirectoryPath(cacheDir: String): Unit = this.cacheDir = cacheDir

  override def loadDefaultSetting: Unit = this.handleDefaultSettingLoad
}

import com.mickey305.openssl.wrapper.scalafx.plugin.{ExcelExporter => Exporter}

class ExcelExporter[T](path: String) extends Exporter[T](path) {
  import scala.collection.JavaConverters._
  import java.util.{function => jfunc}
  /**
    * export excel file
    */
  def execute(lines: ObservableList[T], callback: T => Seq[String]): Unit = {
    val path = super.getPath
    val func: jfunc.Function[T, Array[String]] = log => callback.apply(log).toArray
    if (path.endsWith(".xlsx")) super.execute(lines.toList.asJava, func)
  }
}