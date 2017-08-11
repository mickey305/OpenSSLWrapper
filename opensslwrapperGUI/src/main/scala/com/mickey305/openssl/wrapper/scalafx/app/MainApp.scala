package com.mickey305.openssl.wrapper.scalafx.app

import java.io.IOException
import java.net.URL
import javafx.{scene => jfxs}

import com.mickey305.openssl.wrapper.scalafx.controller.MainAppControllerInterface

import scalafx.application.JFXApp
import scalafx.scene.{Parent, Scene}
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object MainApp extends JFXApp {
  val resource: Option[URL] = Option(getClass.getResource("MainApp.fxml"))
  if (resource.isEmpty) throw new IOException("Cannot load resource: MainApp.fxml")
  val loader = new FXMLLoader(resource.get, NoDependencyResolver)

  loader.load()

  val root = loader.getRoot[jfxs.Parent]
  val controller = loader.getController[MainAppControllerInterface]

  controller.setStage(stage)

  stage = new JFXApp.PrimaryStage() {
    title = "Openssl Wrapper App"
    scene = new Scene(new jfxs.Scene(root))
    resizable = false
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

}