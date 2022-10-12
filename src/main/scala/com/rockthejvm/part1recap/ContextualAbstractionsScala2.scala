package com.rockthejvm.part1recap

import scala.language.implicitConversions

object ContextualAbstractionsScala2 {

  // implicit classes
  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"
  }

  implicit class ImpersonableString(name: String) {
    def greet(): String =
      Person(name).greet()
  }

  //extension method
  val greeting = "Peter".greet()


  // example: scala.concurrent.duration
  import scala.concurrent.duration._
  val oneSecond: FiniteDuration = 1.second

  // implicit arguments and values
  def increment(x: Int)(implicit amount: Int): Int = x + amount
  implicit val defaultAmount: Int = 10
  val twelve: Int = increment(2) // implicit argument 10 passed by the compiler

  def multiply(x: Int)(implicit factor: Int): Int = x * factor
  val aHundred: Int = multiply(10) // same implicit argument passed by the compiler

  // more complex example
  trait JSONSerializer[T] {
    def toJson(value: T): String
  }

  def convert2Json[T](value: T)(implicit serializer: JSONSerializer[T]): String = serializer.toJson(value)

  implicit  val personSerializer: JSONSerializer[Person] = new JSONSerializer[Person] {
    override def toJson(person: Person): String = "{\"name\" : \""  + person.name + "\"}"
  }
  val davidsJson: String = convert2Json(Person("David"))

  // implicit defs: are a way to synthesise implicit values automatically
  implicit def createListSerializer[T](implicit serializer: JSONSerializer[T]): JSONSerializer[List[T]] =
    new JSONSerializer[List[T]] {
      override def toJson(list: List[T]) = s"[${list.map(serializer.toJson).mkString(", ")}]"
    }
  val personsJson: String = convert2Json(List(Person("Alice"), Person("Bob")))

  // implicit conversions (not recommended)
  case class Cat(name: String) {
    def meow(): String = s"$name is meowing"
  }

  implicit def string2Cat(name: String): Cat = Cat(name)
  val aCat: Cat = "Garfield" // string2Cat("Garfield")
  val garfieldMeowing: String = "Garfield".meow()

  def main(args: Array[String]): Unit = {
    println(davidsJson)
    println(personsJson)

  }
}
