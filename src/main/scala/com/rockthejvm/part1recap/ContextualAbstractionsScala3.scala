package com.rockthejvm.part1recap

import com.rockthejvm.part1recap.ContextualAbstractionsScala2.{davidsJson, personsJson}

object ContextualAbstractionsScala3 {
  //given/using combo
  def increment(x: Int)(using amount: Int): Int = x + amount
  given defaultAmount: Int = 10
  val twelve: Int = increment(2) // (10) automatically by the compiler

  def multiply(x: Int)(using factor: Int) = x * factor
  val aHundred = multiply(10) // default amount is passed automatically

  // more complex use case
  trait Combiner[A] {
    def combine(x: A, y: A): A
    def empty: A
  }

  def combineAll[A](values: List[A])(using combiner: Combiner[A]): A =
    values.foldLeft(combiner.empty)(combiner.combine)

  given intCombiner: Combiner[Int] with {
    override def combine(x: Int, y: Int): Int = x + y
    override def empty = 0
  }
  val numbers = (1 to 10).toList
  val sum10 = combineAll(numbers) //int container passed automatically

  // synthesize given instances
  given optionCombiner[T](using combiner: Combiner[T]): Combiner[Option[T]] with {
    override def empty = Some(combiner.empty)
    override def combine(x: Option[T], y: Option[T]): Option[T] = for {
      vx <- x
      vy <- y
    } yield combiner.combine(vx, vy)
  }

  val sumOptions: Option[Int] = combineAll(List(Some(1), None, Some(2)))

  //extension methods
  case class Person(name: String) {
    def greet(): String = s"Hi, my name is $name"

  }

  extension (name: String)
    def greet(): String = Person(name).greet()

  // generic extension
  extension [T](list: List[T])
    def reduceAll(using combiner: Combiner[T]): T =
      list.foldLeft(combiner.empty)(combiner.combine)

  val alicesGreeting = "Alice".greet()

  val sum10_v2 = numbers.reduceAll

  //type classes
  def main(args: Array[String]): Unit = {
    println(davidsJson)
    println(personsJson)

  }
}
