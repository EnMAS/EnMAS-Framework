package org.enmas.client.gui
import org.enmas.client.ClientManager, org.enmas.messaging._, org.enmas.client.Agent,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       javax.swing.table._,
       akka.actor._,
       java.net.InetAddress, java.io._

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
    layout(new FlowPanel(new Label("Server Host: "), serverHostField)) = North
    val serverTableCols = Array("Model Name", "Host", "Port", "Iterating")
    val serverTable = new Table(Array[Array[Any]](), serverTableCols) {
      selection.intervalMode = Table.IntervalMode.Single
      selection.elementMode = Table.ElementMode.Row
    }
    def previousRowSelection = serverTable.selection.rows
    def selectedServerHost = serverTable.model.getValueAt(serverTable.selection.rows.head, 1).toString
    def selectedServerPort = serverTable.model.getValueAt(serverTable.selection.rows.head, 2).asInstanceOf[Int]

    listenTo(serverTable.selection)
    layout(new ScrollPane(serverTable)) = Center
    val connectButton = new Button { action = Action("Connect to Selected") {
      (clientManager ? ClientManager.Init(
        ClientManager.clientPort,
        selectedServerHost,
        selectedServerPort
      )).get match { case true  ⇒ StatusBar.connected }
    }}
    connectButton.enabled = false

    val scanButton = new Button { action = Action("Scan Host") {
      (clientManager ? ClientManager.ScanHost(serverHostField.text.trim)).get match {
        case result: ClientManager.ScanResult  ⇒ {
          println("\ngot "+result.replies.length+" replies\n")
          serverTable.model = new MyTableModel(
            result.replies.map {
              r: DiscoveryReply  ⇒ List(r.modelName, r.host, r.port, r.iterating).toArray
            }.toArray,
            serverTableCols
          )
        }
      }
    }}
    layout(new FlowPanel(connectButton, scanButton)) = South

    reactions += { case TableRowsSelected(serverTable, range, false)  ⇒ {
      if (serverTable.selection.rows != previousRowSelection)
        serverTable.selection.rows --= previousRowSelection
      connectButton.enabled = true
    }}
  })

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
    val launchButton = new Button { action = Action("Launch Client") {
      classListView.selection.items.headOption match {
        case Some(clazz)  ⇒ {
          clientManager ? ClientManager.LaunchAgent('A1, clazz)
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
    layout(launchButton) = South
  })

  lazy val graphicsTab = new TabbedPane.Page("Graphics Clients", new BorderPanel {})
  lazy val loggersTab = new TabbedPane.Page("Logger Clients", new BorderPanel {})

  // these methods are called by the client manager
  def update(iteration: POMDPIteration) {}
  def loadPlugin(plugin: GraphicsPlugin) {}
  def unloadPlugin(plugin: GraphicsPlugin) {}

  // these methods are called by plugins
  def getPane() {}
  def getFrame(width: Int, height: Int) {}
  def getJFrame(width: Int, height: Int) {}
}