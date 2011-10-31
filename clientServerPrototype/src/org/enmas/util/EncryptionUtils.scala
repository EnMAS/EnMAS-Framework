package org.enmas.util

import java.security._, java.security.interfaces._,
       javax.crypto._

object EncryptionUtils {

  def genKeyPair = {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(1024)
    keyGen.genKeyPair
  }

	def createSymKey = {
		val keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(new SecureRandom())
		keyGen.generateKey
	}

  /**
   * @stub
   */
  def asymEncrypt(key: Key, message: Array[Byte]): Array[Byte] = { message }

  /**
   * @stub
   */
  def symEncrypt(key: SecretKey, message: Array[Byte]): Array[Byte] = { message }

  /**
   * @stub
   */
  def symDecrypt(key: SecretKey, message: Array[Byte]): Array[Byte] = { message }

  implicit def key2Str(k: Key): String = {
    new String(k.getEncoded)
  }

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