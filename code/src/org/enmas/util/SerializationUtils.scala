package org.enmas.util

/** @see: https://github.com/jboner/akka/blob/release-1.2/akka-remote/src/main/scala/akka/remote/MessageSerializer.scala
  */
object SerializationUtils {

  import akka.remote.MessageSerializer
  import scala.collection.mutable.ArrayBuilder

  /** Returns the result of serializing any object using
    * Akka's MessageSerializer implementation.
    */
  def serialize(obj: Any): Array[Byte] = {
    val messageProtocol = MessageSerializer.serialize(obj)
    var buff = ArrayBuilder.make[Byte]
    buff ++= str2ByteArray(messageProtocol.getSerializationScheme.toString)
    buff ++= messageProtocol.getMessage.toByteArray
    buff ++= messageProtocol.getMessageManifest.toByteArray
    buff.result
  }

//  def unserialize[T](bytes: Array[Byte]): obj: T = {
//    MessageSerializer.deserialize[T](bytes)
//  }

  /** Facilitates implicit conversion from String to Array[Byte]
    */
  implicit def str2ByteArray(s: String): Array[Byte] = {
    val chars = s.toCharArray
    val bytes = new Array[Byte](chars.length * 2)
    for (i  ← 0 until chars.length) {
      bytes(i * 2) = chars(i).asInstanceOf[Byte]
      bytes(i * 2 + 1) = (chars(i) >> 8).asInstanceOf[Byte]
    }
    bytes
  }

}