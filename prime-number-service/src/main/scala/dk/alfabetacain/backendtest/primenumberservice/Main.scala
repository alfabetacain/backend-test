package dk.alfabetacain.backendtest.primenumberservice

import com.twitter.finagle.{ListeningServer, Thrift}
import com.twitter.util.{Await, Future}
import dk.alfabetacain.backendtest.contract.{InvalidNumber, PrimeNumberService}

object Main extends App {

  def calculatePrimes(number: Int): Either[String, Stream[Int]] = {
    if (number < 0) {
      Left("input must be larger or equal to zero")
    } else {
      val prefix =
        if (number > 0) {
          Stream(1)
        } else {
          Stream.empty
        }
      val res = prefix.append(
        filterPrimes(Stream.from(2))
      )
      Right(res.takeWhile(_ <= number))
    }
  }

  def filterPrimes(s: Stream[Int]): Stream[Int] = {
    s match {
      case Stream.Empty =>
        Stream.empty
      case h #:: t =>
        h #:: filterPrimes(t.filter(_ % h != 0))
    }
  }

  val server: ListeningServer = Thrift.server.serveIface(
    "localhost:8082",
    new PrimeNumberService[Future] {
      override def primes(ceiling: Int): Future[List[Int]] = {
            calculatePrimes(ceiling) match {
              case Left(err) =>
                Future.exception(new InvalidNumber(err))
              case Right(res) =>
                Future.value(res.toList)
        }
      }
    }
  )

  Await.ready(server)
}
