package dk.alfabetacain.backendtest.proxyservice

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFlatSpec with Matchers {
  "prime number calculation" should "be correct for 2-11" in {
    val expected = List(2, 3, 5, 7, 11)
    val calculated = Main.primes(11)
    assert(calculated == expected)
  }
}
