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
      case Some((om: Manifest[_], obj: Any))  ⇒
        if (om <:< m) Some(obj.asInstanceOf[T]) else None
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
      case Some(obj)  ⇒ obj.asInstanceOf[T]
      case _  ⇒ throw new java.util.NoSuchElementException
    }
  }

  /** == Java API Method == */
  def getBoolean(key: String): Option[Boolean] = getAs[Boolean](key)
  /** == Java API Method == */
  def getDouble(key: String): Option[Double] = getAs[Double](key)
  /** == Java API Method == */
  def getFloat(key: String): Option[Float] = getAs[Float](key)
  /** == Java API Method == */
  def getInt(key: String): Option[Int] = getAs[Int](key)
  /** == Java API Method == */
  def getLong(key: String): Option[Long] = getAs[Long](key)
  /** == Java API Method == */
  def getNumber(key: String): Option[Number] = getAs[Number](key)
}

object State {
  def apply() = new State
  private def apply(map: HashMap[String, (Manifest[_], Any)]) = new State(map)
  def empty = State()
}
