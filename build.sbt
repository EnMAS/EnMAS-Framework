name := "EnMAS"

organization := "org.enmas"

version := "0.13.0-SNAPSHOT"

publishTo := Some(Resolver.sftp(
   "EnMAS Repository",
   "repo.enmas.org",
   "/var/www/vhosts/enmas.org/subdomains/repo/httpdocs"
) as("connor"))

fork in run := true

scalaVersion := "2.10.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

retrieveManaged := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "com.typesafe.akka" %% "akka-remote" % "2.1.0",
  "org.scala-lang" % "scala-swing" % "2.10.0",
  "org.scala-lang" % "scala-compiler" % "2.10.0",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "org.scalatest" %% "scalatest" % "2.0.M5b" % "test"
)

scalacOptions in (Compile, doc) ++= Opts.doc.title("EnMAS") 

scalacOptions in Compile ++= Seq("-unchecked", "-deprecation", "-feature")
