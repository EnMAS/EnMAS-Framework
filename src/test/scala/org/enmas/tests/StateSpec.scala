package org.enmas.tests

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.enmas.pomdp._

class StateSpec extends FlatSpec with ShouldMatchers {
  "A State" should "equal another state with identical contents" in {
    val s1 = State("integer"  → 5) + ("float"  → 5f) + ("double"  → 5.0) + ("boolean"  → true)
    val s2 = State("integer"  → 5) + ("float"  → 5f) + ("double"  → 5.0) + ("boolean"  → true)
    s1 should equal (s2)
  }

  it should "not equal another state with different contents" in {
    val s1 = State("integer"  → 5) + ("float"  → 5f) + ("double"  → 5.0) + ("boolean"  → true)
    val s2 = State("integer"  → 5) + ("float"  → 5f) + ("double"  → 5.0) + ("boolean"  → false)
    s1 should not equal (s2)
  }
}