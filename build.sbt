name := "EnMAS"

organization := "org.enmas"

version := "0.6.2"

publishTo := Some(Resolver.sftp(
   "EnMAS Repository",
   "repo.enmas.org",
   "/var/www/vhosts/enmas.org/subdomains/repo/httpdocs"
) as("connor"))

fork in run := true

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

retrieveManaged := true

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0-M3",
  "com.typesafe.akka" % "akka-remote" % "2.0-M3",
  "org.scala-lang" % "scala-swing" % "2.9.1",
  "org.scala-lang" % "scala-compiler" % "2.9.1",
  "org.scala-lang" % "scala-library" % "2.9.1"
)

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "config")
}
