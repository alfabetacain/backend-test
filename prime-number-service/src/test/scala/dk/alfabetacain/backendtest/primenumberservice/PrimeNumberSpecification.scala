package dk.alfabetacain.backendtest.primenumberservice

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAll, propBoolean}

object PrimeNumberSpecification extends Properties("Prime numbers") {

  property("less than 0") = forAll { (n: Int) =>
    (n < 0) ==> {
      Main.calculatePrimes(n) match {
        case Left(_) => true
        case _ => false
      }
    }
  }

  val positiveNumbers: Gen[Int] = for {
    n <- Gen.choose(1, 5000)
  } yield n

  property("larger than 0") = forAll(positiveNumbers) { (n: Int) =>
    (n > 0 && n < 5000) ==> {
      Main.calculatePrimes(n) match {
        case Right(s) =>
          s.forall(isPrime)
        case _ => false
      }
    }
  }

  def isPrime(possiblePrime: Int) = {
    if (possiblePrime < 0) {
      false
    } else {
      possiblePrime == 0 || possiblePrime == 1 || Stream.range(2, possiblePrime).forall(x => possiblePrime == 1 || x == possiblePrime || possiblePrime % x != 0)
    }
  }
}
