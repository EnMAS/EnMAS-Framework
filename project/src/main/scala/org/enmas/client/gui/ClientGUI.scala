package org.enmas.client.gui

import org.enmas.pomdp._, org.enmas.client.ClientManager, org.enmas.messaging._,
       org.enmas.client.Agent, org.enmas.util.ClassLoaderUtils._,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._, akka.pattern.ask,
       java.net.InetAddress

       // for testing only:
       import org.enmas.examples.Simple._

class ClientGUI(application: ActorRef) extends MainFrame {
  import ClientManager._, Modal._

  title = "EnMAS: Client Manager"
  contents = ui
  minimumSize = new Dimension(600, 550)
  centerOnScreen
  visible = true

  private val jarChooser = new FileChooser {
    title = "Choose JAR file"
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false
    fileHidingEnabled = true
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new javax.swing.filechooser.FileFilter {
      def accept(f: java.io.File) = f.getName.endsWith(".jar") || f.getName.endsWith(".JAR")
      def getDescription = "JAR files"
    }
  }

  lazy val ui = new BorderPanel {
    // Left side of the pane
    val leftPanel = new BoxPanel(Orientation.Vertical) {
      border = Swing.EmptyBorder(30, 20, 10, 30)
      minimumSize = new Dimension(300, 300)
      maximumSize = new Dimension(300, 1200)

      val chooseJarButton = new Button { action = Action("Choose JAR file") {
        val result = jarChooser.showDialog(this, "Choose JAR file")
        if (result == FileChooser.Result.Approve && jarChooser.selectedFile.exists) {         
//        pomdpListView.listData = findClasses[POMDP](jarChooser.selectedFile) filterNot {
//          _.getName.contains("$") } map { clazz  ⇒ clazz.newInstance }
          
          // TODO: populate POMDP list from JAR, do code provisioning
          println("TODO: populate POMDP list from JAR, do code provisioning")

        }
      }}

      val pomdpListView = new ListView[POMDP] {
        selection.intervalMode = ListView.IntervalMode.Single
      }
      pomdpListView.listData = Seq(simpleModel)

      listenTo(pomdpListView.selection)
      val pomdpDetails = new TextArea { editable = false; lineWrap = true; wordWrap = true; }
      val launchServerButton = new Button { action = Action("Request new server with selected") {
        pomdpListView.selection.items.headOption match {
          case Some(pomdp: POMDP)  ⇒ {
            application ! CreateServer(rightPanel.serverHostField.text, pomdp)
            rightPanel.scanButton.doClick
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
        (application ? ScanHost(serverHostField.text.trim)).onSuccess {
          case result: DiscoveryReply  ⇒ {
            if (result.servers.isEmpty)
              popup("Scan Result", "The host is up but has no active servers.")
            else  serverListView.listData = result.servers
          }
          case t: Throwable  ⇒ popup("Connection Error", "Unable to reach the specified host.")
        } onFailure { case _  ⇒ popup("Connection Error", "Unable to reach the specified host.") }
      }}

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