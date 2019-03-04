package com.example

import com.example.NumberRegistryActor.ActionPerformed

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat2(Number)
  implicit val usersJsonFormat = jsonFormat1(Numbers)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
