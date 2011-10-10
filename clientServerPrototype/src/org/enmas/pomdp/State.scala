package org.enmas.pomdp

import scala.collection.immutable.HashMap

class State(
  map: HashMap[String, (Manifest[_], Any)] = 
    scala.collection.immutable.HashMap.empty[String, (Manifest[_], Any)]
) extends java.io.Serializable {

  def +[T <: Any](elem: (String, T))(implicit m: Manifest[T]): State =
    State(map.+((elem._1, (m, elem._2))))

  def -(key: String): State = State(map.-(key))

  def keySet = map.keySet

  def getAs[T](key: String)(implicit m : Manifest[T]): Option[T] = {
    map.get(key) match {
      case Some((om: Manifest[_], o: Any)) =>
        if (om <:< m) Some(o.asInstanceOf[T]) else None
      case _ => None
    }
  }

}

object State {
  def apply() = new State()
  def apply(map: HashMap[String, (Manifest[_], Any)]) = new State(map)
  def empty = State()
}
