package dk.alfabetacain.backendtest.proxyservice

import com.twitter.finagle.{Http, Thrift}
import com.twitter.io.Buf
import com.twitter.util.{Await, Future}
import dk.alfabetacain.backendtest.contract.{InvalidNumber, PrimeNumberService}
import dk.alfabetacain.backendtest.contract.PrimeNumberService.Primes
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.iteratee.Enumerator
import io.finch.iteratee._

object Main extends App {

  implicit val e: Encode.Aux[Exception, Text.Plain] = Encode.instance({
    case (e: InvalidNumber, _) =>
      Buf.Utf8(e.getMessage())
    case _ => Buf.Empty
  })

  val client: PrimeNumberService.ServicePerEndpoint =
    Thrift.client.servicePerEndpoint[PrimeNumberService.ServicePerEndpoint](
      "localhost:8082",
      "thrift_client"
    )

  val primeNumbers: Endpoint[Enumerator[Future, Int]] =
    get("prime" :: path[Int].withToString("number")) { number: Int =>
      val result = client.primes(Primes.Args(number))
      result.map(res => {
        Ok(
          enumStream[Int](res.toStream)
        )
      }).handle{
        case e: InvalidNumber => BadRequest(e)
      }
    }

  Await.ready(Http.server.serve(":8081", primeNumbers.toService))
}

