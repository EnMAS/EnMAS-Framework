package org.enmas.tests

import org.enmas.pomdp.State
import org.enmas.pomdp.State.Implicits._

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class StateSpec extends FlatSpec with ShouldMatchers {

  // setup

  val s1 = State(
    "integer" -> 5,
    "float" -> 5f,
    "double" -> 5.0,
    "boolean" -> true
  )

  val s2 = State(
    "boolean" -> true,
    "double" -> 5.0,
    "float" -> 5f,
    "integer" -> 5
  )

  val s3 = State(
    "integer" -> 5,
    "float" -> 5f,
    "double" -> 5.0,
    "boolean" -> false
  )

  // tests

  "State" should "initialize without elements" in {
    val s = State()
    s.map.size should equal (0)
  }

  it should "initialize with one element" in {
    val s = State("myKey" -> 555)
    s.map.size should equal (1)
    s.getAs[Int]("myKey") should equal (Some(555))
  }

  it should "return the values saved into it" in {
    s1.getAs[Int]("integer") should equal (Some(5))
    s1.getAs[Float]("float") should equal (Some(5f))
    s1.getAs[Double]("double") should equal (Some(5.0))
    s1.getAs[Boolean]("boolean") should equal (Some(true))
  }

  it should "be equal to itself" in {
    s1 should equal (s1)
    s2 should equal (s2)
    s3 should equal (s3)
  }

  it should "equal another state with identical contents" in {
    s1 should equal (s2)
    s2 should equal (s1)
  }

  it should "not equal another state with different contents" in {
    s1 should not equal (s3)
    s3 should not equal (s2)
  }

  it should "behave properly with respect to hashCode" in {
    s1.hashCode should equal(s2.hashCode)
    s2.hashCode should not equal (s3.hashCode)
  }

}