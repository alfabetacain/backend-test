package dk.alfabetacain.backendtest.proxyservice

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.http.javadsl.model
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import dk.alfabetacain.backendtest.grpc.{PrimeNumberService, PrimeNumberServiceClient, PrimeReply, PrimeRequest}

import scala.io.StdIn

object Main extends App {

  implicit val sys = ActorSystem("Proxy")
  implicit val mat = ActorMaterializer()
  implicit val ec = sys.dispatcher

  val clientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 8080).withTls(false)

  val client: PrimeNumberService = PrimeNumberServiceClient(clientSettings)

  implicit val primeReplyMarshaller = Marshaller.strict[PrimeReply, ByteString] { reply =>
    Marshalling.WithFixedContentType(ContentTypes.`text/plain(UTF-8)`, () => {
      ByteString(reply.value.toString)
    })
  }

  implicit val primeReplyStreaming = new CustomEntityStreaming()

  val route =
     {
      get {
        pathPrefix("primes" / IntNumber) { ceiling =>
          complete(client.getPrimes(PrimeRequest(ceiling)))
        }
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

final class CustomEntityStreaming(maxLineLength: Int, val supported: ContentTypeRange, val contentType: ContentType, val framingRenderer: Flow[ByteString, ByteString, NotUsed], val parallelism: Int, val unordered: Boolean) extends EntityStreamingSupport {

  def this() =
    this(
      1024,
      ContentTypeRange(ContentTypes.`text/plain(UTF-8)`),
      ContentTypes.`text/plain(UTF-8)`,
      Flow[ByteString].intersperse(ByteString(",")),
      1, false
    )

  override def framingDecoder: Flow[ByteString, ByteString, NotUsed] = Framing.delimiter(ByteString(","), maxLineLength)

  override def withSupported(range: model.ContentTypeRange): EntityStreamingSupport = ???

  override def withContentType(range: model.ContentType): EntityStreamingSupport = ???

  override def withParallelMarshalling(parallelism: Int, unordered: Boolean): EntityStreamingSupport = ???
}
