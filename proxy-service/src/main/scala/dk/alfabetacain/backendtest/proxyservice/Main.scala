package dk.alfabetacain.backendtest.proxyservice

import com.twitter.finagle.{Http, Thrift}
import com.twitter.util.{Await, Future}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.iteratee.Enumerator
import io.finch.iteratee._
import thrift.PrimeNumberService
import thrift.PrimeNumberService.Primes

object Main extends App {

  final case class Numbers(value: List[Int])

  def primes(number: Int): Stream[Int] = {
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

  val client: PrimeNumberService.ServicePerEndpoint =
    Thrift.client.servicePerEndpoint[PrimeNumberService.ServicePerEndpoint](
      "localhost:8082",
      "thrift_client"
    )


  val primeNumbers: Endpoint[Enumerator[Future, Int]] =
    get("prime" :: path[Int].withToString("number")) { number: Int =>
      val result = client.primes(Primes.Args(11))
      result.map(res => {
        println("res = " + res)
        Ok(
          enumStream[Int](res.toStream)
        )
      })
    }

  Await.ready(Http.server.serve(":8081", primeNumbers.toService))
}

