name := "EnMAS"

organization := "org.enmas"

version := "0.10"

publishTo := Some(Resolver.sftp(
   "EnMAS Repository",
   "repo.enmas.org",
   "/var/www/vhosts/enmas.org/subdomains/repo/httpdocs"
) as("connor"))

fork in run := true

scalaVersion := "2.9.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

retrieveManaged := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0.2",
  "com.typesafe.akka" % "akka-remote" % "2.0.2",
  "org.scala-lang" % "scala-swing" % "2.9.2",
  "org.scala-lang" % "scala-compiler" % "2.9.2",
  "org.scala-lang" % "scala-library" % "2.9.2",
  "net.databinder" %% "unfiltered-filter-async" % "0.6.1",
  "net.databinder" %% "unfiltered-jetty" % "0.6.1",
  "net.databinder" %% "unfiltered-netty" % "0.6.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.scalatest" %% "scalatest" % "1.7.2" % "test"
)

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "config")
}

scalacOptions in Compile ++= Seq("-unchecked")
