package org.enmas.pomdp

import scala.collection.immutable.HashMap,
       scala.reflect._,
       scala.reflect.runtime.universe._,
       scala.language.implicitConversions,
       scala.language.existentials

/**
  * Represents the state of a POMDP.
  *
  * This structure is a type-safe wrapper for a totally generic HashMap.
  * The State stores type information for mapped objects.  When retrieving
  * objects, the proper type must be supplied.
  */
class State(
  val map: HashMap[String, (TypeTag[_], Any)] = 
    scala.collection.immutable.HashMap.empty[String, (TypeTag[_], Any)]
) extends java.io.Serializable {

  /** Returns a new State that contains a mapping from elem._1 to elem._2
    */
  def +[T <: Any : TypeTag](elem: (String, T)): State =
    State(map.+((elem._1, (typeTag[T], elem._2))))

  /** Returns a new State which contains no mapping from key
    */
  def -(key: String): State = State(map.-(key))

  /** Returns the set of Strings that map to some object in this State
    */
  def keySet = map.keySet

  /** Returns a Some[T] object iff key is mapped and the mapped object
    * conforms to the supplied type T.  Returns None otherwise.
    */
  def getAs[T : TypeTag](key: String): Option[T] = {
    map.get(key) match {
      case Some((tt, obj)) =>
        if (tt.tpe <:< typeTag[T].tpe) Some(obj.asInstanceOf[T]) else None
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
      (s: String, mapping: (String, (TypeTag[_], Any))) => {
        val (key, value) = mapping
        s + "  %s -> %s\n".format(key, value._2.toString)
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

  def apply[T : TypeTag](mapping: (String, T)): State = 
    State().+(mapping)(typeTag[T])

  def apply(mappings: AugmentedElem[_]*): State = {
    mappings.foldLeft(new State) {
      (state, aElem) =>
        val AugmentedElem(key, value, tt) = aElem
        state.+(key -> value)(tt.asInstanceOf[TypeTag[Any]])
    }
  }

  private def apply(map: HashMap[String, (TypeTag[_], Any)]) = new State(map)

  private[pomdp] case class AugmentedElem[T](
    key: String,
    value: T,
    tt: TypeTag[T]
  )

  object Implicits {

    implicit def elem2AugmentedElem[T: TypeTag](
      mapping: (String, T)
    ): AugmentedElem[T] = {
      val (key, value) = mapping
      AugmentedElem(key, value, typeTag[T])
    }

  }
}
