package org.enmas.util.voodoo

import java.io._, java.net._, java.lang.reflect._, java.util.jar._

object ClassLoaderUtils {

  /** To be mixed in with classes that need to be able to receive
    * the raw byte content of a JAR file over the network and add
    * the classes therein to the system classloader.
    */
  trait Provisionable {
    import org.enmas.pomdp._, org.enmas.util.FileUtils._, java.io._
    /** Black Magic! Saves the incoming FileData bytes as a JAR file
      * and adds the classes within to a custom local classloader,
      * returning the created JAR File object and a list of objects
      * of the classes within conforming to this method's type parameter.
      *
      * Note that this method requires the classes that conform to this
      * method's type parameter to have a zero argument constructor.
      */
    def provision[T](fileData: FileData)(implicit m: scala.reflect.Manifest[T]): (Option[File], List[T]) = {
      verifyFileData(fileData) match {
        case Some(data)  ⇒ {
          val jarFile = File.createTempFile("provisioned", ".jar")
          val fout = new FileOutputStream(jarFile)
          fout.write(data)
          fout.flush
          val ts = findSubclasses[T](jarFile) filterNot {
            _.getName.contains("$") } map { clazz  ⇒ clazz.newInstance }
          (Some(jarFile), ts)
        }
        case None  ⇒ (None, List[T]())
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

  /** 
    */
  def findClasses[T](file: File)(implicit m: scala.reflect.Manifest[T]) = {
    val jarFile = new JarFile(file)
    val jarEntries = jarFile.entries
    var classes = List[java.lang.Class[T]]()
    while (jarEntries.hasMoreElements) {
      val entry = jarEntries.nextElement
      if (entry.getName.endsWith(".class")) {
        val name = entry.getName.replace(".class", "")
        try {
          val clazz = getClass(file, name)
          classes :+= clazz.asInstanceOf[java.lang.Class[T]]
        }
        catch { case _  ⇒ () }
      }
    }
    classes
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