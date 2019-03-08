package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.example.WebServer.{host, port}

import scala.concurrent.Future

object Put extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def post(data: Int): Unit = {
    val responseFuture: Future[HttpResponse] =
      Http(system).singleRequest(
        HttpRequest(
          HttpMethods.PUT,
          s"http://$host:$port/num?num=$data",
          entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, data.toString.getBytes())
        )
      )
    println(s"PUT number $data successfully")
  }

  val numbers = Source(1 to 1000)
  numbers.runForeach(i ⇒ post(i))(materializer)
}
