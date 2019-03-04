package com.example

import akka.actor.{Actor, ActorLogging, Props}

import scala.annotation.tailrec

final case class Number(key: String, value: Int)
final case class Numbers(numbers: Seq[Number])

object NumberRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetNumbers
  final case class CreateNumber(number: Number)
  final case class GetNumber(key: String)
  final case class DeleteNumber(key: String)

  def props: Props = Props[NumberRegistryActor]
}

class NumberRegistryActor extends Actor with ActorLogging {
  import NumberRegistryActor._



  var numbers = Set.empty[Number]

  def receive: Receive = {
    case GetNumbers =>
      var sum = 0
      var sumSeq = numbers.foreach(x => sum += x.value)
      sender() ! Numbers(Seq(Number(s"$sum", sum)))
    case CreateNumber(number) =>
      numbers += number
      sender() ! ActionPerformed(s"Number with key ${number.key} created.")
    case GetNumber(key) =>
      sender() ! numbers.find(_.key == key)
    case DeleteNumber(key) =>
      numbers.find(_.key == key) foreach { number => numbers -= number }
      sender() ! ActionPerformed(s"Number with key ${key} deleted.")
  }
}