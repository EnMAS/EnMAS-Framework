package org.enmas.client.gui

import org.enmas.pomdp.State
import scala.swing._, java.io.File

trait EnMAS_GUI {

  protected def getBaseDirectory: File = {
  	val classDirUrl = State.getClass.getResource("State.class")
  	val classDirPath = classDirUrl.getPath.replaceAll("%20", " ")
  	val baseDirPath = classDirPath.substring(
  	  classDirPath.indexOf('/'),
  	  classDirPath.indexWhere(
  	    (c: Char) => { c == '/' },
  	    classDirPath.lastIndexOfSlice("EnMAS")
  	  )
  	)
  	new File(baseDirPath)
  }

  protected def createJarFileChooser = new FileChooser(getBaseDirectory) {
    title = "Choose JAR file"
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false
    fileHidingEnabled = true
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new javax.swing.filechooser.FileFilter {
      def accept(f: java.io.File) = {
        f.isDirectory || 
        f.getName.endsWith(".jar") || 
        f.getName.endsWith(".JAR")
      }
      def getDescription = "JAR files"
    }
  }

  def popup(headline: String, message: String) {
    val dialog = new Dialog {
      title = headline
      contents = new FlowPanel(new Label(message)) { vGap = 60; hGap = 60; }
      centerOnScreen
      resizable = false
      visible = true
      override def closeOperation = dispose
    }
  }

  def confirm(headline: String, message: String): Boolean = {
    // TODO: spawn a popup confirmation box and return
    //       the user decision as a Boolean
    true
  }

}