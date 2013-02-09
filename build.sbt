name := "EnMAS"

organization in ThisBuild := "org.enmas"

version := "0.13.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.0"

retrieveManaged in ThisBuild := true

libraryDependencies in ThisBuild ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
  "org.scala-lang" % "scala-reflect" % "2.10.0"
)

scalacOptions in (ThisBuild, Compile) ++= Seq("-unchecked", "-deprecation", "-feature")
