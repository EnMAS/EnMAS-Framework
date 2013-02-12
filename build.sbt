name := "EnMAS"

organization in ThisBuild := "org.enmas"

version := "1.0.0"

scalaVersion in ThisBuild := "2.10.0"

retrieveManaged in ThisBuild := true

libraryDependencies in ThisBuild ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
  "org.scala-lang" % "scala-reflect" % "2.10.0"
)

scalacOptions in (ThisBuild, Compile) ++= Seq("-unchecked", "-deprecation", "-feature")

publishMavenStyle in ThisBuild := true

useGpg in ThisBuild := true

pomIncludeRepository in ThisBuild := { _ => false }

licenses in ThisBuild := Seq(
  "BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php")
)

homepage in ThisBuild := Some(url("http://enmas.org"))

pomExtra in ThisBuild := (
  <scm>
    <url>git@github.com:EnMAS/EnMAS.git</url>
    <connection>scm:git:git@github.com:EnMAS/EnMAS.git</connection>
  </scm>
  <developers>
    <developer>
      <id>ConnorDoyle</id>
      <name>Connor Doyle</name>
      <url>http://topology.io</url>
    </developer>
  </developers>
)

publishTo in ThisBuild <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
