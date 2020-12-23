package dk.alfabetacain.backendtest.primenumberservice

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFlatSpec with Matchers {
  "prime number calculation" should "be correct for 2-11" in {
    val expected = List(1, 2, 3, 5, 7, 11)
    val input = 11
    val calculated = Main.calculatePrimes(input)

    calculated match {
      case Left(err) => fail("Calculation should work for input " + input)
      case Right(s) =>
        assert(s.toList == expected)
    }

  }
}
