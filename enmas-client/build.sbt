import AssemblyKeys._ // put this at the top of the file

assemblySettings

name := "enmas-client"

version := "1.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-remote" % "2.1.0",
  "org.scala-lang" % "scala-swing" % "2.10.0"
)

mainClass in (Compile, run) := Some("org.enmas.client.ClientManager")

mainClass in assembly := Some("org.enmas.client.ClientManager")

jarName in assembly <<= (name, version) { (n, v) => n + "_" + v + "_assembly.jar" }

test in assembly := {}

artifact in (Compile, assembly) ~= { art =>
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case x => old(x)
  }
}
