package com.mickey305.openssl.wrapper.scalafx.app

import java.io.IOException
import javafx.{scene => jfxs}

import com.mickey305.openssl.wrapper.scalafx.controller.MainAppControllerInterface

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object MainApp extends JFXApp {
  val resource = getClass.getResource("MainApp.fxml")
  if (resource == null)
    throw new IOException("Cannot load resource: MainApp.fxml")
  val loader = new FXMLLoader(resource, NoDependencyResolver)

  loader.load()

  val root = loader.getRoot[jfxs.Parent]
  val controller = loader.getController[MainAppControllerInterface]

  controller.setStage(stage)

  stage = new JFXApp.PrimaryStage() {
    title = "Openssl Wrapper App"
    scene = new Scene(new jfxs.Scene(root))
    resizable = false
  }

}