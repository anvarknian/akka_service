package com.example

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import com.example.NumberRegistryActor._

trait NumberRoutes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[NumberRoutes])

  def userRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val userRoutes: Route =
    pathPrefix("numbers") {
      concat(
        pathEnd {
          concat(
            get {
              val numbers: Future[Numbers] =
                (userRegistryActor ? GetNumbers).mapTo[Numbers]
                complete(numbers)
            },
            post {
              entity(as[Number]) { number =>
                val numberCreated: Future[ActionPerformed] =
                  (userRegistryActor ? CreateNumber(number)).mapTo[ActionPerformed]
                onSuccess(numberCreated) { performed =>
                  log.info("Created number [{}]: {}", number.key, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        path(Segment) { key =>
          concat(
            get {
              val maybeNumber: Future[Option[Number]] =
                (userRegistryActor ? GetNumber(key)).mapTo[Option[Number]]
              rejectEmptyResponse {
                complete(maybeNumber)
              }
            },
            delete {
              val numberDeleted: Future[ActionPerformed] =
                (userRegistryActor ? DeleteNumber(key)).mapTo[ActionPerformed]
              onSuccess(numberDeleted) { performed =>
                log.info("Deleted number [{}]: {}", key, performed.description)
                complete((StatusCodes.OK, performed))
              }
            }
          )
        }
      )
    }
}
