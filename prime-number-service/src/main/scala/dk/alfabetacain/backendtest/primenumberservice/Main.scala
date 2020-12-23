package dk.alfabetacain.backendtest.primenumberservice

import com.twitter.finagle.{ListeningServer, Thrift}
import com.twitter.util.{Await, Future}
import dk.alfabetacain.backendtest.contract.PrimeNumberService

object Main extends App {

  def calculatePrimes(number: Int): Stream[Int] = {
    val input = Stream.range(2, number + 1)
    next(input)
  }

  def next(s: Stream[Int]): Stream[Int] = {
    s match {
      case Stream.Empty =>
        Stream.empty
      case h #:: t =>
        Stream(h).append(next(
          t.filter(_ % h != 0)
        ))
    }
  }

  val server: ListeningServer = Thrift.server.serveIface(
    "localhost:8082",
    new PrimeNumberService[Future] {
      override def primes(ceiling: Int): Future[List[Int]] = {
        Future.value(
          calculatePrimes(ceiling).toList
        )
      }
    }
  )

  Await.ready(server)
}
