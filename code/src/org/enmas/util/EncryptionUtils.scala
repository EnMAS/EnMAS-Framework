package org.enmas.util

import java.security._, java.security.interfaces._,
       javax.crypto._

object EncryptionUtils {

  /** Returns a new RSA key.
    */
  def genKeyPair = {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(1024)
    keyGen.genKeyPair
  }

  /** Returns a new AES key.
    */
	def genSymKey = {
		val keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(new SecureRandom())
		keyGen.generateKey
	}

  /** Returns the result of asymmetric encryption of message using key.
    * @stub
    */
  def asymEncrypt(key: Key, message: Array[Byte]): Array[Byte] = { message }

  /** Returns the result of symmetric encryption of message using key.
    * @stub
    */
  def symEncrypt(key: SecretKey, message: Array[Byte]): Array[Byte] = { message }

  /** Returns the result of symmetric decryption of message using key.
    * @stub
    */
  def symDecrypt(key: SecretKey, message: Array[Byte]): Array[Byte] = { message }

  /** Facilitates implicit conversion of a Key to a String.
    */
  implicit def key2Str(k: Key): String = {
    new String(k.getEncoded)
  }
}