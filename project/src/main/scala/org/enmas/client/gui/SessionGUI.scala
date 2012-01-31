package org.enmas.client.gui

import org.enmas.client._, org.enmas.messaging._,
       scala.swing._, scala.swing.event._, scala.swing.BorderPanel.Position._,
       akka.actor._, akka.dispatch._, akka.util.duration._, akka.pattern.ask,
       java.io._

class SessionGUI(session: ActorRef, server: ServerSpec) extends Frame {
  import ClientManager._, Session._, Modal._

  title = "EnMAS: Session Manager"
  contents = ui
  minimumSize = new Dimension(500, 600)
  visible = true

  override def closeOperation = session ! Kill

  lazy val ui = {
    new BorderPanel {
      layout(new TabbedPane {
        pages ++= List(agentsTab, iterationSubscribersTab)
      }) = Center
    }
  }

  private val jarChooser = new FileChooser {
    title = "Choose JAR file"
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false
    fileHidingEnabled = true
    peer.setAcceptAllFileFilterUsed(false)
    fileFilter = new javax.swing.filechooser.FileFilter {
      def accept(f: File) = f.getName.endsWith(".jar") || f.getName.endsWith(".JAR")
      def getDescription = "JAR files"
    }
  }

  lazy val agentsTab = new TabbedPane.Page("Agents",
    new GridPanel(2, 1) {
      val top = new BorderPanel {
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

        val agentTypeCombo = new ComboBox(
          for (c  ← server.pomdp.agentConstraints) yield c.agentType
        )

        val launchButton = new Button { action = Action("Launch Agent") {
          classListView.selection.items.headOption match {
            case Some(clazz)  ⇒ {
              val agentType = agentTypeCombo.selection.item
              (session ? LaunchAgent(agentType, clazz)) onSuccess {
                case confirmation: ConfirmAgentRegistration  ⇒ {
                  bottom.agentListView.listData ++= Seq(confirmation)
                }
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
        layout(new FlowPanel(new Label("Choose JAR to Search for Agents:"), chooseJarButton)) = North
        layout(new ScrollPane(classListView)) = Center
        layout(new BorderPanel {
          layout(new FlowPanel(
            new Label("Agent Type"),
            agentTypeCombo,
            launchButton
          )) = Center
        }) = South
      }

      val bottom = new BorderPanel {
        val agentListView = new ListView[ConfirmAgentRegistration] {
          selection.intervalMode = ListView.IntervalMode.Single
        }
        listenTo(agentListView.selection)

        val killButton = new Button { action = Action("Kill Selected Agent") {
          agentListView.selection.items.headOption match {
            case Some(item)  ⇒ {
              session ! KillAgent(item.agentNumber)
              agentListView.listData = agentListView.listData filterNot { _ == item }
              enabled = false
            }
            case None  ⇒ ()
          }
        }}

        layout(new Label("Active Agents for this Session:")) = North
        layout(new ScrollPane(agentListView)) = Center
        layout(killButton) = South
        reactions += { case event: ListSelectionChanged[_]  ⇒ {
          killButton.enabled = true
        }}
      }

      contents ++= Seq(top, bottom)
    }
  )

  lazy val iterationSubscribersTab = new TabbedPane.Page("Iteration Subscriber Clients",
    new GridPanel(2, 1) {
      val top = new BorderPanel {
        val classListView = new ListView[Class[_ <: IterationClient]] {
          selection.intervalMode = ListView.IntervalMode.Single
        }
        listenTo(classListView.selection)

        val chooseJarButton = new Button { action = Action("Choose JAR file") {
          val result = jarChooser.showDialog(this, "Choose JAR file")
          if (result == FileChooser.Result.Approve && jarChooser.selectedFile.exists) {
            import org.enmas.util.ClassLoaderUtils._
            classListView.listData = findSubclasses[IterationClient](jarChooser.selectedFile)
          }
        }}

        val launchButton = new Button { action = Action("Launch Client") {
          classListView.selection.items.headOption match {
            case Some(clazz)  ⇒ {
              (session ? LaunchClient(clazz)) onSuccess {
                case confirmation: ConfirmClientRegistration  ⇒ {
                  bottom.subscriberListView.listData ++= Seq(confirmation)
                }
                case _  ⇒ popup(
                  "Failure",
                  "Failed to launch the client."
                )
              } onFailure { case _  ⇒ popup(
                "Failure",
                "Failed to launch the client."
              )}
            }
            case None  ⇒ enabled = false
          }
        }}
        launchButton.enabled = false
        reactions += { case event: ListSelectionChanged[_]  ⇒ {
          launchButton.enabled = true
        }}
        layout(new FlowPanel(new Label("Choose JAR to Search for Subscriber Clients:"), chooseJarButton)) = North
        layout(new ScrollPane(classListView)) = Center
        layout(launchButton) = South
      }

      val bottom = new BorderPanel {
        val subscriberListView = new ListView[ConfirmClientRegistration] {
          selection.intervalMode = ListView.IntervalMode.Single
        }
        listenTo(subscriberListView.selection)

        val killButton = new Button { action = Action("Kill Selected Client") {
          subscriberListView.selection.items.headOption match {
            case Some(item)  ⇒ {
              session ! KillClient(item.clientNumber)
              subscriberListView.listData = subscriberListView.listData filterNot { _ == item }
              enabled = false
            }
            case None  ⇒ ()
          }
        }}

        layout(new Label("Active Subsctriber Clients for this Session:")) = North
        layout(new ScrollPane(subscriberListView)) = Center
        layout(killButton) = South
        reactions += { case event: ListSelectionChanged[_]  ⇒ {
          killButton.enabled = true
        }}
      }

      contents ++= Seq(top, bottom)
    }
  )  
}