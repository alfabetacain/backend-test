package dk.alfabetacain.backendtest.proxyservice

import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.finch.syntax._

object Main extends App {

  final case class Numbers(value: List[Int])

  def primes(number: Int): Stream[Int] = {
    val input = Stream.range(2, number + 1)
    next(input)
  }

  def next(s: Stream[Int]): Stream[Int] = {
    s.headOption match {
      case None => Stream.empty
      case Some(p) =>
        Stream(p).append(next(
          s.drop(1).filter(_ % p != 0)
        ))
    }
  }

  val primeNumbers: Endpoint[Numbers] =
    get("/prime/" :: path[Int]) { number: Int =>
      Ok(Numbers(List(number)))
    }

  Await.ready(Http.server.serve(":8081", primeNumbers.toService))
}

