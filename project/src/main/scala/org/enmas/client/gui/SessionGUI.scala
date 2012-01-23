package org.enmas.client.gui

import org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.client.Agent, org.enmas.util.ServerSpec,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._,
       java.io._

class SessionGUI(session: ActorRef) extends Frame {
  import ClientManager._, Modal._

  title = "EnMAS: Session Manager"
  contents = ui
  minimumSize = new Dimension(700, 800)
  visible = true

  override def closeOperation = session ! Kill

  lazy val ui = {
    new BorderPanel {
      layout(new TabbedPane {
        pages ++= List(agentsTab, graphicsTab, loggersTab)
      }) = Center
    }
  }

  object StatusBar extends Label {
    opaque = true
    connected
    def disconnected = {
      text = "Server Status: Not Connected"
      background = new Color(255, 239, 0)
    }
    def connected = {
      text = "Server Status: Connected"
      background = new Color(32, 255, 32)
    }
  }

  lazy val agentsTab = new TabbedPane.Page("Agent Clients", new BorderPanel {
    val jarChooser = new FileChooser {
      title = "Choose jar file"
      fileSelectionMode = FileChooser.SelectionMode.FilesOnly
      multiSelectionEnabled = false
      fileHidingEnabled = true
      peer.setAcceptAllFileFilterUsed(false)
      fileFilter = new javax.swing.filechooser.FileFilter {
        def accept(f: File) = f.getName.endsWith(".jar") || f.getName.endsWith(".JAR")
        def getDescription = "JAR files"
      }
    }

    val classListView = new ListView[Class[_ <: Agent]] {
      selection.intervalMode = ListView.IntervalMode.Single
    }
    listenTo(classListView.selection)

    val chooseJarButton = new Button { action = Action("Choose JAR file") {
      val result = jarChooser.showDialog(this, "Choose JAR file")
      if (result == FileChooser.Result.Approve && jarChooser.selectedFile.exists) {
        import org.enmas.util.ClassLoaderUtils._
        classListView.listData = findSubclasses[Agent](jarChooser.selectedFile)
      }
    }}

    val agentTypeField = new TextField("", 16)

    val launchButton = new Button { action = Action("Launch Client") {
      classListView.selection.items.headOption match {
        case Some(clazz)  ⇒ {
          val agentType = Symbol(agentTypeField.text)
          (session ? LaunchAgent(agentType, clazz)) onSuccess {
            case true  ⇒ popup(
              "Success",
              "Launched agent of type "+agentType
            )
            case _  ⇒ popup(
              "Failure",
              "The server declined to admit an agent of type "+agentType+"."
            )
          } onFailure { case _  ⇒ popup(
            "Failure",
            "Failed to launch the agent.  There was a problem contacting the server."
          )}
        }
        case None  ⇒ enabled = false
      }
    }}
    launchButton.enabled = false
    reactions += { case event: ListSelectionChanged[_]  ⇒ {
      launchButton.enabled = true
    }}
    layout(chooseJarButton) = North
    layout(classListView) = Center
    layout(new BorderPanel {
      layout(new FlowPanel(
        new Label("Agent Type"),
        agentTypeField,
        launchButton
      )) = Center
      layout(StatusBar) = South
    }) = South
  })

  lazy val graphicsTab = new TabbedPane.Page("Graphics Clients", new BorderPanel {})
  lazy val loggersTab = new TabbedPane.Page("Logger Clients", new BorderPanel {})
  
}