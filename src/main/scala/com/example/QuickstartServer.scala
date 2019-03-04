package com.example

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.io.StdIn

object QuickstartServer extends App with NumberRoutes {

  val PostReq = s"""curl -H "Content-type: application/json" -X POST -d '{"key": "31", "value": 31}' http://0.0.0.0:8080/numbers"""

  implicit val system: ActorSystem = ActorSystem("AkkaHTTPServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val userRegistryActor: ActorRef = system.actorOf(NumberRegistryActor.props, "userRegistryActor")

  lazy val routes: Route = userRoutes
  val host= "0.0.0.0"
  val port = 8080
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)

  println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  serverBinding
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}

