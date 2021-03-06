package org.enmas.client.gui

import org.enmas.pomdp.POMDP,
       org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.pomdp.Agent, org.enmas.util.voodoo.ClassLoaderUtils._,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.pattern.ask,
       scala.concurrent.duration._,
       scala.concurrent.ExecutionContext.Implicits.global,
       java.net.InetAddress

class ClientGUI(application: ActorRef) extends MainFrame with EnMAS_GUI {
  import ClientManager._

  title = "EnMAS: Client Manager"
  contents = ui
  minimumSize = new Dimension(600, 550)
  centerOnScreen
  visible = true
  refreshPOMDPs

  private def refreshPOMDPs {
    (application ? GetLocalPOMDPs) onSuccess {
      case POMDPList(pomdps) => {
        ui.leftPanel.pomdpListView.listData = pomdps
      }
    }
  }

  private val jarChooser = createJarFileChooser

  lazy val ui = new BorderPanel {
    // Left side of the pane
    val leftPanel = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(30, 20, 10, 30)
      minimumSize = new Dimension(300, 300)
      maximumSize = new Dimension(300, 1200)

      val chooseJarButton = new Button { action = Action("Choose JAR file") {
        val result = jarChooser.showDialog(this, "Choose JAR file")
        if (result == FileChooser.Result.Approve && jarChooser.selectedFile.exists) {
          application ! LoadPOMDPsFromFile(jarChooser.selectedFile)
          refreshPOMDPs
        }
      }}

      val pomdpListView = new ListView[POMDP] {
        selection.intervalMode = ListView.IntervalMode.Single
      }

      listenTo(pomdpListView.selection)
      val pomdpDetails = new TextArea { editable = false; lineWrap = true; wordWrap = true; }
      val launchServerButton = new Button { action = Action("Request new server with selected") {
        pomdpListView.selection.items.headOption match {
          case Some(pomdp: POMDP) => {
            application ! CreateServer(rightPanel.serverHostField.text, pomdp.getClass.getName)
            rightPanel.scanButton.doClick
          }
          case None => popup("Server Launch Error", "No POMDP selected!")
        }
      }}
      launchServerButton.enabled = false

      reactions += { case event: ListSelectionChanged[_] => {
        pomdpListView.selection.items.headOption match {
          case Some(pomdp: POMDP) => {
            pomdpDetails.text = pomdp.description.replaceAll("\\r|\\n", " ").trim
            pomdpDetails.caret.position = 0
          }
          case None => ()
        }
        launchServerButton.enabled = true
      }}

      contents ++= Seq(
        new FlowPanel(new Label("Choose JAR to Search for POMDPs"), chooseJarButton),
        new Label("Available POMDP models:"),
        new ScrollPane(pomdpListView),
        new Label("Selected POMDP details:"),
        new ScrollPane(pomdpDetails) {
          horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
        },
        launchServerButton
      )
    }

    // Right side of the pane
    val rightPanel = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(30, 10, 20, 30)
      minimumSize = new Dimension(300, 300)

      val serverHostField = new TextField(InetAddress.getLocalHost.getHostAddress, 16)

      val scanButton = new Button { action = Action("Scan Host") {
        serverListView.listData = List[ServerSpec]()
        serverDetails.text = ""
        connectButton.enabled = false
        val futureResult = (application ? ScanHost(serverHostField.text.trim))

        futureResult onSuccess {
          case reply: DiscoveryReply => {
            if (reply.servers.isEmpty)
              popup("Scan Result", "The host is up but has no active servers.")
            else  serverListView.listData = reply.servers
          }
        }
        futureResult onFailure { case _ => {
          popup("Scan Result", "Could not find a host at the specified address."); 
        }}
      }}

      val serverListView = new ListView[ServerSpec] {
        selection.intervalMode = ListView.IntervalMode.Single
      }
      listenTo(serverListView.selection)
      val serverDetails = new TextArea { editable = false; lineWrap = true; wordWrap = true; }

      val connectButton = new Button { action = Action("Connect to Selected") {
        serverListView.selection.items.headOption match {
          case Some(server: ServerSpec) => (application ? CreateSession(server)) onFailure {
            case _ => popup("Connection Error", "Unable to connect to the specified server.")
          }
          case None => popup("Connection Error", "No server selected!")
        }
      }}
      connectButton.enabled = false

      reactions += { case event: ListSelectionChanged[_] => {
        serverListView.selection.items.headOption match {
          case Some(server: ServerSpec) => {
            serverDetails.text = server.pomdpDescription.replaceAll("\\r|\\n", " ").trim
            serverDetails.caret.position = 0
          }
          case None => ()
        }
        connectButton.enabled = true
      }}

      contents ++= Seq(
        new FlowPanel(new Label("Server Host: "), serverHostField, scanButton),
        new Label("Active server instances:"),
        new ScrollPane(serverListView),
        new Label("Selected server details:"),
        new ScrollPane(serverDetails) {
          horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
        },
        connectButton
      )
    }

    layout(new GridPanel(1, 2) {
      contents ++= Seq(leftPanel, rightPanel)
    }) = Center
  }
}