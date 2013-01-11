name := "EnMAS"

organization := "org.enmas"

version := "0.13.0-SNAPSHOT"

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
  "com.typesafe.akka" % "akka-actor" % "2.0.4",
  "com.typesafe.akka" % "akka-remote" % "2.0.4",
  "org.scala-lang" % "scala-swing" % "2.9.2",
  "org.scala-lang" % "scala-compiler" % "2.9.2",
  "org.scala-lang" % "scala-library" % "2.9.2",
  "net.databinder" %% "unfiltered-filter-async" % "0.6.1",
  "net.databinder" %% "unfiltered-jetty" % "0.6.1",
  "net.databinder" %% "unfiltered-netty" % "0.6.1",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "org.scalatest" %% "scalatest" % "1.7.2" % "test"
)

scalacOptions in Compile ++= Seq("-unchecked")
