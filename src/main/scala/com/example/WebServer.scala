package com.example

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.io.StdIn

object WebServer {

  case object GetNums


  class Auction extends Actor with ActorLogging {
    var listOfAllNumbers: List[Int] = List.empty

    def receive = {
      case num: Int =>
        listOfAllNumbers = listOfAllNumbers :+ num
        log.info(s"\n Sum of numbers : ${listOfAllNumbers.sum} \n")
      case GetNums => sender() ! listOfAllNumbers.sum
      case _ => log.info("\nInvalid message\n")
    }
  }


  val host = "localhost"
  val port = 8080

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem()
    val auction = system.actorOf(Props[Auction], "Sum")

    implicit val materializer: ActorMaterializer = ActorMaterializer()


    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val route =
      post {
        pathPrefix("num") {
          parameter("num".as[Int]) { num =>
            // place a bid, fire-and-forget
            auction ! num
            complete((StatusCodes.Accepted, s"\n$num added\n"))
          }
        }
      } ~
        put {
          pathPrefix("num") {
            parameter("num".as[Int]) { num =>
              // place a bid, fire-and-forget
              auction ! num
              complete((StatusCodes.Accepted, s"\n$num added\n"))
            }
          }
        } ~
        get {
          pathPrefix("sum") {
            implicit val timeout: Timeout = 0.seconds
            val result = Await.result(auction ? GetNums, 0 seconds)
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"\nThe sum is equal to $result\n"))
          }
        }

    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

    println(s"\nServer online at http://localhost:8080/\nPress RETURN to stop...\n")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}