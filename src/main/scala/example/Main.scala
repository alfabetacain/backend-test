package example

import com.twitter.finagle.Http
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.syntax._
import io.finch.circe._
import io.finch._

object Main extends App {

  case class Locale(language: String, country: String)
  case class Time(locale: Locale, time: String)

  def currentTime(locale: java.util.Locale): String =
    java.util.Calendar.getInstance(locale).getTime.toString

  val time: Endpoint[Time] =
    post("time" :: jsonBody[Locale]) { locale: Locale =>
      Ok(Time(locale, currentTime(new java.util.Locale(locale.language, locale.country))))
    }

  Await.ready(Http.server.serve(":8081", time.toService))
}

