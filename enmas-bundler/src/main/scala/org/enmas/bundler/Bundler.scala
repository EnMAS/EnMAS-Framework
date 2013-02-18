package org.enmas.bundler

import org.enmas.pomdp.POMDP,
       scala.tools.nsc._, scala.tools.nsc.reporters._,
       scala.swing._, scala.swing.event._, 
       scala.swing.BorderPanel.Position._,
       scala.concurrent.{future, Future},
       scala.concurrent.ExecutionContext.Implicits.global,
       scala.util.{Try, Success, Failure},
       java.io._, java.net._, java.util.jar._

class Bundler extends MainFrame {
  title = "EnMAS: Jar Bundler"
  contents = ui
  minimumSize = new Dimension(650, 400)
  centerOnScreen
  visible = true

  lazy val ui = new BorderPanel {

    val sourceChooser = new FileChooser {
      title = "Choose source directory"
      fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
      multiSelectionEnabled = false
      fileHidingEnabled = true
      peer.setAcceptAllFileFilterUsed(false)
      fileFilter = new javax.swing.filechooser.FileFilter {
        def accept(f: File) = f.isDirectory
        def getDescription = "Only directories"
      }
    }

    object StatusBar extends Label {
      opaque = true
      noSource
      def noSource = {
        text = "Please choose a source directory."
        background = new Color(223, 223, 223)
      }
      def working = {
        text = "Working..."
        background = new Color(255, 239, 0)
      }
      def failure = {
        text = "Compilation Failed."
        background = new Color(255, 64, 64)
      }
      def success = {
        text = "Compilation Succeeded."
        background = new Color(32, 255, 32)
      }
    }

    val results = new TextArea
    results.editable = false

    val compileSourceButton = new Button {
      action = Action("Compile scala source files...") {
        results.text = ""
        val v = sourceChooser.showDialog(this, "Choose source directory")
        if (v != FileChooser.Result.Approve || ! sourceChooser.selectedFile.exists)
          StatusBar.noSource
        else {
          clean(sourceChooser.selectedFile)
          compile(sourceChooser.selectedFile) map {
            case Success(message) => {
              StatusBar.success
              results.text += message
            }
            case Failure(cause) => {
              StatusBar.failure
              results.text += "%s:\n%s".format(cause.getClass.getName, cause.getMessage)
            }
          }
          StatusBar.working
        }
      }
    }

    def clean(sourceDir: File) {
      results.text += "Cleaning directory of .class files...\n"
      findFiles(
        sourceDir,
        (f: File) => f.toString.endsWith(".class")
      ) map { _.delete() }
    }

    def compile(sourceDir: File): Future[Try[String]] = future { Try {
      results.text += "Compiling source files...\n"
      val settings = new Settings()
      import java.net.{URL, URLClassLoader}
      val urls = ClassLoader.getSystemClassLoader.asInstanceOf[URLClassLoader].getURLs
      settings.classpath.value = urls.map(_.getFile).mkString(":")
      settings.sourcepath.value = sourceDir.getPath
      results.text += "Source directory: [%s]\n" format settings.sourcepath.value
      settings.outdir.value = settings.sourcepath.value

      val sourceFiles = findFiles(
        sourceDir,
        (f: File) => f.toString.endsWith(".scala")
      )

      val errorLog = new StringWriter
      val reporter = new ConsoleReporter(settings, null, new PrintWriter(errorLog))
      val scalac = new Global(settings, reporter)
      new scalac.Run compile sourceFiles.map(_.getAbsolutePath).toList

      if (reporter.hasErrors) throw new RuntimeException(errorLog.toString)
      else {
        makeJar(sourceDir)
        "Compilation completed without errors.\n\n"
      }
    }}

    def makeJar(sourceDir: File) {
      results.text += "Making JAR file... "
      import java.util.jar._

      val jarFile = new File(sourceDir.getParentFile, sourceDir.getName.replaceAll("\\s", "")+".jar")
      jarFile.delete
      jarFile.getParentFile.mkdirs
      val manifest = new java.util.jar.Manifest
      manifest.getMainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
      val jar = new JarOutputStream(new FileOutputStream(jarFile), manifest)

      val classFiles = findFiles(
        sourceDir,
        (f: File) => f.toString.endsWith(".class")
      )

      results.text += classFiles.length + " class files found\n"
      classFiles.foreach { f =>
        var className = f.getPath.replace(sourceDir.getPath, "").replace("\\", "/").stripPrefix("/")
        results.text += "Bundling class: " + className.replaceAll("/", ".") + "\n"
        jar putNextEntry { new JarEntry(className) }
        val fin = new FileInputStream(f)
        org.enmas.util.IOUtils.copy(fin, jar)
        fin.close
      }
      jar.close
      clean(sourceDir)
    }

    layout(new FlowPanel(compileSourceButton)) = North
    layout(new ScrollPane(results)) = Center
    layout(StatusBar) = South
  }

  def findFiles(dir: File, criterion: (File) => Boolean): Seq[File] = {
    if (dir.isFile) Seq()
    else {
      val (files, dirs) = dir.listFiles.partition(_.isFile)
      files.filter(criterion) ++ dirs.toSeq.map(findFiles(_, criterion)).foldLeft(Seq[File]())(_ ++ _)
    }
  }

}

object Bundler extends App {
  val bundler = new Bundler()
}
