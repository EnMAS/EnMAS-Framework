package org.enmas.client.gui
import org.enmas.client.ClientManager, org.enmas.messaging._,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       javax.swing.table._,
       akka.actor._,
       java.net.InetAddress

class ClientGUI(clientManager: ActorRef) extends MainFrame {
  title = "EnMAS: Client Manager"
  contents = ui
  minimumSize = new Dimension(650, 400)
  centerOnScreen
  visible = true

  object StatusBar extends Label {
    opaque = true
    disconnected
    def disconnected = {
      text = "Server Status: Not Connected"
      background = new Color(255, 239, 0)
    }
    def connected = {
      text = "Server Status: Connected"
      background = new Color(32, 255, 32)
    }
  }

  lazy val ui = {
    new BorderPanel {
      layout(new TabbedPane {
        pages ++= List(connectionTab, agentsTab, graphicsTab, loggersTab)
      }) = Center
      layout(StatusBar) = South
    }
  }

  lazy val connectionTab = new TabbedPane.Page("Connection", new BorderPanel {
    val serverHostField = new TextField(ClientManager.clientHost, 16)
    val serverPortField = new TextField("36627", 5)
    layout(new FlowPanel(new Label("Server Host: "), serverHostField,
      new Label("Server Port: "), serverPortField)) = North
    val serverTableCols = Array("Model Name", "Host", "Port", "Iterating")
    val serverTable = new Table(Array[Array[Any]](), serverTableCols) {
      selection.intervalMode = Table.IntervalMode.Single
      selection.elementMode = Table.ElementMode.Row
    }
    var previousRowSelection = serverTable.selection.rows

    listenTo(serverTable.selection)
    layout(new ScrollPane(serverTable)) = Center
    val connectButton = new Button { action = Action("Connect to Selected") {
      (clientManager ? ClientManager.Init(
        ClientManager.clientPort,
        serverHostField.text.trim,
        serverPortField.text.toInt
      )).get match { case true  ⇒ StatusBar.connected }
    }}

    val scanButton = new Button {action = Action("Scan Host") {
      (clientManager ? ClientManager.ScanHost(
          serverHostField.text.trim,
          serverPortField.text.toInt
      )).get match {
        case result: ClientManager.ScanResult  ⇒ {
          serverTable.model = new MyTableModel(
            result.replies.map {
              reply: DiscoveryReply  ⇒ List(
                reply.modelName,
                reply.host,
                reply.port,
                reply.iterating
              ).toArray
            }.toArray,
            serverTableCols
          )
          println("\n** REPLIES: **\n"+result.replies+"\n")
        }
        case obj: Any  ⇒ println("No result, got this thing instead:\n" + obj)
      }
    }}
    layout(new FlowPanel(connectButton, scanButton)) = South

    reactions += {
      case TableRowsSelected(serverTable, range, false)  ⇒ {
        if (serverTable.selection.rows != previousRowSelection)
          serverTable.selection.rows --= previousRowSelection
        println("Selected row " + serverTable.selection.rows.head)
      }
    }
  })

  lazy val agentsTab = new TabbedPane.Page("Agent Clients", new BorderPanel {})
  lazy val graphicsTab = new TabbedPane.Page("Graphics Clients", new BorderPanel {})
  lazy val loggersTab = new TabbedPane.Page("Logger Clients", new BorderPanel {})

  def getPane() {}
  def getFrame(width: Int, height: Int) {}
  def getJFrame(width: Int, height: Int) {}
  def update(iteration: POMDPIteration) {}
  def loadPlugin(plugin: GraphicsPlugin) {}
  def unloadPlugin(plugin: GraphicsPlugin) {}
}