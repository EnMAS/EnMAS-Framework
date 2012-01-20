package org.enmas.client.gui

import org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.client.Agent, org.enmas.server.ServerSpec,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._,
       java.net.InetAddress

class ClientGUI(application: ActorRef) extends MainFrame {
  import ClientManager._

  title = "EnMAS: Client Manager"
  contents = ui
  minimumSize = new Dimension(600, 550)
  centerOnScreen
  visible = true

  lazy val ui = new BorderPanel {
    val serverHostField = new TextField(InetAddress.getLocalHost.getHostAddress, 16)

    val scanButton = new Button { action = Action("Scan Host") {
      serverListView.listData = List[ServerSpec]()
      connectButton.enabled = false
      (application ? ScanHost(serverHostField.text.trim)).onSuccess {
        case result: ScanResult  ⇒ {
          if (result.servers.isEmpty)
            popup("Scan Result", "The host is up but has no active servers.")
          else  serverListView.listData = result.servers
        }
        case t: Throwable  ⇒ popup("Connection Error", "Unable to reach the specified host.")
      } onFailure { case _  ⇒ popup("Connection Error", "Unable to reach the specified host.") }
    }}

    layout(new FlowPanel(new Label("Server Host: "), serverHostField, scanButton)) = North
    val serverListView = new ListView[ServerSpec] {
      selection.intervalMode = ListView.IntervalMode.Single
    }
    listenTo(serverListView.selection)
    layout(new ScrollPane(serverListView)) = Center

    val connectButton = new Button { action = Action("Connect to Selected") {
      serverListView.selection.items.headOption match {
        case Some(server: ServerSpec)  ⇒ (application ? CreateSession(server.ref)).onFailure {
          case _  ⇒ popup("Connection Error", "Unable to connect to the specified server.")
        }
        case None  ⇒ popup("Connection Error", "No server selected!")
      }
    }}
    connectButton.enabled = false
    layout(connectButton) = South

    reactions += { case event: ListSelectionChanged[_]  ⇒ {
      connectButton.enabled = true
    }}
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

  // these methods are called by the client manager
  def update(iteration: POMDPIteration) {}
  def loadPlugin(plugin: GraphicsPlugin) {}
  def unloadPlugin(plugin: GraphicsPlugin) {}

  // these methods are called by plugins
  def getPane() {}
  def getFrame(width: Int, height: Int) {}
  def getJFrame(width: Int, height: Int) {}
}