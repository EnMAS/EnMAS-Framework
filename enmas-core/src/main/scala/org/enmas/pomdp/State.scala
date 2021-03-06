package org.enmas.pomdp

import scala.collection.immutable.HashMap,
       scala.reflect._,
       // scala.reflect.runtime.universe._,
       scala.language.implicitConversions,
       scala.language.existentials

/**
  * Represents the state of a POMDP.
  *
  * This structure is a type-safe wrapper for a totally generic HashMap.
  * The State stores type information for mapped objects.  When retrieving
  * objects, the proper type must be supplied.
  *
  * Note: This class should uses ClassTags instead of TypeTags due to a bug
  * in Scala 2.10 where TypeCreators are not serializable.
  */
class State(
  val map: HashMap[String, (ClassTag[_], Any)] = 
    scala.collection.immutable.HashMap.empty[String, (ClassTag[_], Any)]
) extends java.io.Serializable {

  /** Returns a new State that contains a mapping from elem._1 to elem._2
    */
  def +[T <: Any : ClassTag](elem: (String, T)): State =
    State(map.+((elem._1, (classTag[T], elem._2))))

  /** Returns a new State which contains no mapping from key
    */
  def -(key: String): State = State(map.-(key))

  /** Returns the set of Strings that map to some object in this State
    */
  def keySet = map.keySet

  /** Returns a Some[T] object iff key is mapped and the mapped object
    * conforms to the supplied type T.  Returns None otherwise.
    */
  def getAs[T : ClassTag](key: String): Option[T] = {
    map.get(key) match {
      case Some((ct, obj)) =>
        if (ct <:< classTag[T]) Some(obj.asInstanceOf[T]) else None
      case _ => None
    }
  }

  /** == Java API Method ==
    *
    * This version of the getAs method should not be called from Scala code.
    * This version is less safe: it throws exceptions and does not take
    * advantage of Scala's Option monad.  This overloaded method definition
    * is provided solely for compatibility with Java client code.
    */
  @throws(classOf[ClassCastException])
  @throws(classOf[java.util.NoSuchElementException])
  def getAs[T](key: String, prototype: T): T = {
    getAs[Any](key) match {
      case Some(obj) => obj.asInstanceOf[T]
      case None => throw new java.util.NoSuchElementException
    }
  }

  /** == Java API Method == */
  def getBoolean(key: String): Boolean = getAs(key, true)
  /** == Java API Method == */
  def getDouble(key: String): Double = getAs(key, 0d)
  /** == Java API Method == */
  def getFloat(key: String): Float = getAs(key, 0f)
  /** == Java API Method == */
  def getInt(key: String): Int = getAs(key, 0)
  /** == Java API Method == */
  def getLong(key: String): Long = getAs(key, 0l)

  /** Returns a String representation of the mappings contained
    * in this State.
    */
  override def toString(): String = {
    map.toTraversable.foldLeft("State(\n") {
      (s: String, mapping: (String, (ClassTag[_], Any))) => {
        val (key, (_, value)) = mapping
        s + "  %s -> %s\n".format(key, value)
      }
    } + ")"
  }

  /** Overridden to defer to the backing hashmap, which
    * performs a proper structural equality test.
    */
  override def equals(obj: Any) = obj match {
    case s2: State => map == s2.map
    case _ => false
  }

  /** Overridden to defer to the backing hashmap to maintain
    * the relationship between equals and hashcode.
    */
    override def hashCode = map.hashCode

}

object State {

  def empty = State()

  def apply(): State = new State

  def apply[T : ClassTag](mapping: (String, T)): State = 
    State().+(mapping)(classTag[T])

  def apply(mappings: AugmentedElem[_]*): State = {
    mappings.foldLeft(new State) {
      (state, aElem) =>
        val AugmentedElem(key, value, ct) = aElem
        state.+(key -> value)(ct.asInstanceOf[ClassTag[Any]])
    }
  }

  private def apply(map: HashMap[String, (ClassTag[_], Any)]) = new State(map)

  private[pomdp] case class AugmentedElem[T](
    key: String,
    value: T,
    ct: ClassTag[T]
  )

  object Implicits {

    implicit def elem2AugmentedElem[T: ClassTag](
      mapping: (String, T)
    ): AugmentedElem[T] = {
      val (key, value) = mapping
      AugmentedElem(key, value, classTag[T])
    }

  }
}
