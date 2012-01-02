import java.io.File, java.util.Scanner
import scala.tools.nsc._, scala.tools.nsc.reporters._

object ScalacDriver extends App {
  val in = new Scanner(System.in)
  print("Source directory? "); val srcDir = new File(in.next.trim)
  if (! srcDir.exists || ! srcDir.isDirectory)
    throw new IllegalArgumentException("Invalid source dir.")
  print("Build directory? "); val buildDir = new File(in.next.trim)
  buildDir.mkdirs
  if (! buildDir.exists || ! buildDir.isDirectory)
    throw new IllegalArgumentException("Build dir did not exist and creating it failed.")

  class CrappyCodeException(msg: String) extends RuntimeException(msg)

  val settings = new Settings()
  settings.sourcepath.value = srcDir.getPath
  settings.outdir.value = buildDir.getPath

	val classDirURL = getClass.getResource("ScalacDriver.class")
	val classDirPath = classDirURL.getPath.replaceAll("%20", " ")
	val baseDir = new File(
	  classDirPath.substring(classDirPath.indexOf("/"), classDirPath.indexOf("EnMAS.jar"))
	)
  settings.classpath.value = baseDir.getPath+"/lib/akka/akka-actor-1.2.jar"

  object scalac extends Global(settings)
  val run = new scalac.Run
  run compile(
    srcDir.listFiles.toList.filter(_.toString.endsWith(".scala")) map { _.getAbsolutePath }
  )
  if (scalac.reporter.hasErrors) throw new CrappyCodeException("You should do better.")
}