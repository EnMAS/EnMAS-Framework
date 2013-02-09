import sbt._
import Keys._

object EnMASBuild extends Build {

  lazy val root = Project(id = "enmas", base = file(".")) aggregate(
    enmasBundler,
    enmasClient,
    enmasCore,
    enmasExamples,
    enmasServer
  )

  lazy val enmasCore = Project(
    id = "enmas-core",
    base = file("enmas-core")
  )

  lazy val enmasBundler = Project(
    id = "enmas-bundler",
    base = file("enmas-bundler")
  ) dependsOn(
    enmasCore
  )

  lazy val enmasClient = Project(
    id = "enmas-client",
    base = file("enmas-client")
  ) dependsOn(
    enmasCore
  )

  lazy val enmasServer = Project(
    id = "enmas-server",
    base = file("enmas-server")
  ) dependsOn(
    enmasCore
  )

  lazy val enmasExamples = Project(
    id = "enmas-examples",
    base = file("enmas-examples")
  ) dependsOn(
    enmasCore
  )

}