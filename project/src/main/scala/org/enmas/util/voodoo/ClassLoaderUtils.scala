package org.enmas.util.voodoo

import java.io._, java.net._, java.lang.reflect._, java.util.jar._

object ClassLoaderUtils {

  /** To be mixed in with classes that need to be able to receive
    * the raw byte content of a JAR file over the network.
    */
  trait Provisionable {
    import org.enmas.pomdp._, org.enmas.util.FileUtils._, java.io._
    /** Saves the incoming FileData bytes as a temp file
      * if the computed MD5 matches the attached checksum,
      * returning the optional File object.
      */
    def provision[T]
      (fileData: FileData)
      (implicit m: scala.reflect.Manifest[T])
    : Option[File] = {
      verifyFileData(fileData) match {
        case Some(data)  ⇒ {
          val file = File.createTempFile("provisioned", null)
          file.deleteOnExit
          val fout = new FileOutputStream(file)
          fout.write(data)
          fout.flush
          Some(file)
        }
        case None  ⇒ None
      }
    }
  }

  def findSubclasses[T](file: File)(implicit m: scala.reflect.Manifest[T]) = {
    val jarFile = new JarFile(file)
    val jarEntries = jarFile.entries
    var subclasses = List[java.lang.Class[_ <: T]]()
    while (jarEntries.hasMoreElements) {
      val entry = jarEntries.nextElement
      if (entry.getName.endsWith(".class")) {
        val name = entry.getName.replace(".class", "")
        try {
          val clazz = getClass(file, name)
          clazz.asSubclass(m.erasure)
          subclasses :+= clazz.asInstanceOf[java.lang.Class[_ <: T]]
        }
        catch { case _  ⇒ () }
      }
    }
    subclasses
  }

  private def getClass(file: File, name: String): java.lang.Class[_] = {
    addURL(file.toURI.toURL)
    getClass().getClassLoader.asInstanceOf[URLClassLoader].loadClass(name)
  }

  private def addURL(u: URL): Unit = {
    val sysLoader = getClass().getClassLoader.asInstanceOf[URLClassLoader]
    val urls = sysLoader.getURLs
    var alreadyHasURL = urls.contains { url: URL  ⇒ url.toString.equalsIgnoreCase(u.toString) }
    if (! alreadyHasURL) {
      sysLoader.getClass.getMethods.filter{ _.getName == "addURL" }.headOption match {
        case Some(m)  ⇒ { m setAccessible true; m.invoke(sysLoader, u) }
        case None  ⇒ ()
      }
    }
  }

}