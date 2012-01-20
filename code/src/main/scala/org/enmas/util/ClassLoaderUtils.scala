package org.enmas.util

import java.io._, java.net._, java.lang.reflect._, java.util.jar._

object ClassLoaderUtils {

  def findSubclasses[T](file: File)(implicit m: scala.reflect.Manifest[T]) = {
    val jarFile = new JarFile(file)
    val jarEntries = jarFile.entries
    var subclasses = List[java.lang.Class[_ <: T]]()
    while (jarEntries.hasMoreElements) {
      val entry = jarEntries.nextElement
      if (entry.getName.endsWith(".class")) {
        val className = entry.getName
        val name = className.slice(0, className.indexOf(".class"))
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