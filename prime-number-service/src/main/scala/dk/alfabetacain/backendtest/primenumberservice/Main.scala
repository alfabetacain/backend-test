package dk.alfabetacain.backendtest.primenumberservice

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import dk.alfabetacain.backendtest.grpc.{PrimeNumberService, PrimeNumberServiceHandler, PrimeReply, PrimeRequest}

import scala.concurrent.{ExecutionContext, Future}

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
        Stream.cons(h, filterPrimes(t.filter(_ % h != 0)))
    }
  }

  override def main(args: Array[String]): Unit = {
    val conf = ConfigFactory
      .parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())
    val system = ActorSystem("HelloWorld", conf)
    new PrimeServer(system).run()
  }
}

class PrimeServer(system: ActorSystem) {
  def run(): Future[Http.ServerBinding] = {
    implicit val sys: ActorSystem = system
    implicit val mat: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = sys.dispatcher

    val service: HttpRequest => Future[HttpResponse] =
      PrimeNumberServiceHandler(new Service())

    val binding = Http().bindAndHandleAsync(
      service,
      interface = "127.0.0.1",
      port = 8080,
      connectionContext = HttpConnectionContext()
    )

    binding.foreach{ binding => println(s"bound to ${binding.localAddress}")}
    binding
  }
}
class Service(implicit mat: Materializer) extends PrimeNumberService {

  override def getPrimes(req: PrimeRequest): Source[PrimeReply, NotUsed] = {
    Source(Main.calculatePrimes(req.upperLimit).getOrElse(Stream.empty)).map(x => PrimeReply(x))
  }

}
