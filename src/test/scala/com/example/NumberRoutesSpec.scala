package com.example

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class NumberRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with NumberRoutes {

  override val userRegistryActor: ActorRef =
    system.actorOf(NumberRegistryActor.props, "numberRegistry")

  lazy val routes = userRoutes


  "NumberRoutes" should {
    "return no numbers if no present (GET /numbers)" in {
      val request = HttpRequest(uri = "/numbers")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"numbers":[{"key":"0","value":0}]}""")
      }
    }

    "be able to add users (POST /numbers)" in {
      val user = Number("42", 42)
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/numbers").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Number with key 42 created."}""")
      }
    }

    "be able to remove numbers (DELETE /numbers)" in {
      val request = Delete(uri = "/numbers/42")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Number with key 42 deleted."}""")
      }
    }
  }
}

