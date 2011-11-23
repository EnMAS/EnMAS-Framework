package org.enmas.pomdp

import scala.collection.immutable.HashMap, scala.reflect._

/** Represents the state of a POMDP.
  *
  * This structure is a type-safe wrapper for a totally generic HashMap.
  * The State stores type information for mapped objects.  When retrieving
  * objects, the proper type must be supplied.
  */
class State(
  map: HashMap[String, (Manifest[_], Any)] = 
    scala.collection.immutable.HashMap.empty[String, (Manifest[_], Any)]
) extends java.io.Serializable {

  /** @post Returns a new State that contains a mapping from elem._1 to elem._2
    */
  def +[T <: Any](elem: (String, T))(implicit m: Manifest[T]): State =
    State(map.+((elem._1, (m, elem._2))))

  /** @post Returns a new State which contains no mapping from key
    */
  def -(key: String): State = State(map.-(key))

  /** Returns the set of Strings that map to some object in this State
    */
  def keySet = map.keySet

  /** Returns a Some[T] object iff key is mapped and the mapped object
    * conforms to the supplied type T.  Returns None otherwise.
    */
  def getAs[T](key: String)(implicit m : Manifest[T]): Option[T] = {
    map.get(key) match {
      case Some((om: Manifest[_], o: Any))  ⇒
        if (om <:< m) Some(o.asInstanceOf[T]) else None
      case _  ⇒ None
    }
  }

  /** == Java API Method ==
    *
    * This version of the getAs method should not be called from Scala code.
    * This version is less safe: it throws exceptions and does not take
    * advantage of Scala's Option monad.  This overloaded method definition
    * is provided solely for compatibility with Java client code.
    */
  @throws(classOf[java.util.NoSuchElementException])
  def getAs[T <: AnyRef](key: String, prototypeObject: T): T = {
    val clazz = prototypeObject.getClass.asInstanceOf[java.lang.Class[T]]
    getAs(key)(Manifest.classType(clazz)) match {
      case Some(o)  ⇒ o.asInstanceOf[T]
      case _  ⇒ throw new java.util.NoSuchElementException
    }
  }

}

object State {
  def apply() = new State
  private def apply(map: HashMap[String, (Manifest[_], Any)]) = new State(map)
  def empty = State()
}
