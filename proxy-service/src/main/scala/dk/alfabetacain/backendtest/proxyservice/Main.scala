package dk.alfabetacain.backendtest.proxyservice

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.http.javadsl.model
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import dk.alfabetacain.backendtest.grpc.{PrimeNumberService, PrimeNumberServiceClient, PrimeRequest}

import scala.io.StdIn

object Main extends App {

  implicit val sys = ActorSystem("Proxy")
  implicit val mat = ActorMaterializer()
  implicit val ec = sys.dispatcher

  val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8080).withTls(false)

  val client: PrimeNumberService = PrimeNumberServiceClient(clientSettings)

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, client.getPrimes(PrimeRequest(4)).map(x => ByteString(s"${x.value}"))))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8081)

  StdIn.readLine()
  bindingFuture.flatMap(_.unbind())
    .onComplete(_ => sys.terminate())

  /*
  val requests = List(1, 2, 3).map(PrimeRequest(_))
  val reply = client.getPrimes(requests.head).runForeach(rep => println("got reply " + rep))

  reply.onComplete{
    case Success(_) =>
      println("Done!")
    case Failure(e) =>
      println(s"Error = $e")
  }
   */
}

class CustomEntityStreaming extends EntityStreamingSupport {
  override def supported: ContentTypeRange = ???

  override def contentType: ContentType = ???

  override def framingDecoder: Flow[ByteString, ByteString, NotUsed] = ???

  override def framingRenderer: Flow[ByteString, ByteString, NotUsed] = ???

  override def withSupported(range: model.ContentTypeRange): EntityStreamingSupport = ???

  override def withContentType(range: model.ContentType): EntityStreamingSupport = ???

  override def parallelism: Int = ???

  override def unordered: Boolean = ???

  override def withParallelMarshalling(parallelism: Int, unordered: Boolean): EntityStreamingSupport = ???
}
