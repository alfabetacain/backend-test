package dk.alfabetacain.backendtest.proxyservice

import com.twitter.finagle.{Http, Thrift}
import com.twitter.util.{Await, Future}
import dk.alfabetacain.backendtest.contract.PrimeNumberService
import dk.alfabetacain.backendtest.contract.PrimeNumberService.Primes
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.iteratee.Enumerator
import io.finch.iteratee._

object Main extends App {

  final case class Numbers(value: List[Int])

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

