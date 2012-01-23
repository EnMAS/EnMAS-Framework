package org.enmas.client.gui

import org.enmas.pomdp._, org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.client.Agent, org.enmas.util.ServerSpec,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._,
       java.net.InetAddress
       
// for testing:
import org.enmas.examples.Broadcast._, org.enmas.examples.Simple._

class ClientGUI(application: ActorRef) extends MainFrame {
  import ClientManager._, Modal._

  title = "EnMAS: Client Manager"
  contents = ui
  minimumSize = new Dimension(600, 550)
  centerOnScreen
  visible = true

  lazy val ui = new BorderPanel {
    val serverHostField = new TextField(InetAddress.getLocalHost.getHostAddress, 16)

    val scanButton = new Button { action = Action("Scan Host") {
      leftPanel.serverListView.listData = List[ServerSpec]()
      leftPanel.serverDetails.text = ""
      leftPanel.connectButton.enabled = false
      (application ? ScanHost(serverHostField.text.trim)).onSuccess {
        case result: DiscoveryReply  ⇒ {
          if (result.servers.isEmpty)
            popup("Scan Result", "The host is up but has no active servers.")
          else  leftPanel.serverListView.listData = result.servers
        }
        case t: Throwable  ⇒ popup("Connection Error", "Unable to reach the specified host.")
      } onFailure { case _  ⇒ popup("Connection Error", "Unable to reach the specified host.") }
    }}

    layout(new FlowPanel(new Label("Server Host: "), serverHostField, scanButton)) = North

    // Left side of the pane
    val leftPanel = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(30, 10, 20, 30)
      minimumSize = new Dimension(300, 300)

      val serverListView = new ListView[ServerSpec] {
        selection.intervalMode = ListView.IntervalMode.Single
      }
      listenTo(serverListView.selection)
      val serverDetails = new TextArea { editable = false; lineWrap = true; wordWrap = true; }

      val connectButton = new Button { action = Action("Connect to Selected") {
        serverListView.selection.items.headOption match {
          case Some(server: ServerSpec)  ⇒ (application ? CreateSession(server)).onFailure {
            case _  ⇒ popup("Connection Error", "Unable to connect to the specified server.")
          }
          case None  ⇒ popup("Connection Error", "No server selected!")
        }
      }}
      connectButton.enabled = false

      reactions += { case event: ListSelectionChanged[_]  ⇒ {
        serverListView.selection.items.headOption match {
          case Some(server: ServerSpec)  ⇒ {
            serverDetails.text = server.pomdp.description.replaceAll("\\r|\\n", " ").trim
            serverDetails.caret.position = 0
          }
          case None  ⇒ ()
        }
        connectButton.enabled = true
      }}

      contents ++= Seq(
        new Label("Active server instances:"),
        new ScrollPane(serverListView),
        new Label("Selected server details:"),
        new ScrollPane(serverDetails) {
          horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
        },
        connectButton
      )
    }

    // Right side of the pane
    val rightPanel = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(30, 20, 10, 30)
      minimumSize = new Dimension(300, 300)
      maximumSize = new Dimension(300, 1200)

      val pomdpListView = new ListView[POMDP] {
        selection.intervalMode = ListView.IntervalMode.Single
      }

      // for testing:
      pomdpListView.listData = List(broadcastProblem, simpleModel)

      listenTo(pomdpListView.selection)
      val pomdpDetails = new TextArea { editable = false; lineWrap = true; wordWrap = true; }
      val launchServerButton = new Button { action = Action("Request new server with selected") {
        pomdpListView.selection.items.headOption match {
          case Some(pomdp: POMDP)  ⇒ {
            application ! CreateServer(serverHostField.text, pomdp)
            scanButton.doClick
          }
          case None  ⇒ popup("Server Launch Error", "No POMDP selected!")
        }
      }}
      launchServerButton.enabled = false

      reactions += { case event: ListSelectionChanged[_]  ⇒ {
        pomdpListView.selection.items.headOption match {
          case Some(pomdp: POMDP)  ⇒ {
            pomdpDetails.text = pomdp.description.replaceAll("\\r|\\n", " ").trim
            pomdpDetails.caret.position = 0
          }
          case None  ⇒ ()
        }
        launchServerButton.enabled = true
      }}

      contents ++= Seq(
        new Label("Available POMDP models:"),
        new ScrollPane(pomdpListView),
        new Label("Selected POMDP details:"),
        new ScrollPane(pomdpDetails) {
          horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
        },
        launchServerButton
      )
    }

    layout(new BoxPanel(Orientation.Horizontal) {
      contents ++= Seq(leftPanel, rightPanel)
    }) = Center
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