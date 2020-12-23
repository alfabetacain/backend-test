package dk.alfabetacain.backendtest.proxyservice

import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._
import io.iteratee.Enumerator
import io.finch.iteratee._

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

  val primeNumbers: Endpoint[Enumerator[Future, Int]] =
    get("prime" :: path[Int].withToString("number")) { number: Int =>
      Ok(
        enumStream[Int](primes(number))
      )
    }

  Await.ready(Http.server.serve(":8081", primeNumbers.toService))
}

