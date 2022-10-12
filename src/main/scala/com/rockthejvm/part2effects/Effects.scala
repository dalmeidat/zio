package com.rockthejvm.part2effects

import scala.io.StdIn
import scala.util.Try

object Effects {
  // functional programming
  // EXPRESSIONS
  def combine(a: Int, b: Int): Int = a + b

  // local reasoning = type signature describes the kind of computations that will be performed

  // referential transparency = ability to replace an expression with the value it evaluates to
  val five = combine(2, 3)
  val five_v2 = 2 + 3
  val five_v3 = 5

  // not all expresssions are RT
  val resultOfPrinting: Unit = println("Learning ZIO")
  val resultOfPrinting_v2: Unit = ()

  // example 2: changing a variable
  var anInt = 0
  val changingInt: Unit = (anInt = 42) // side effect
  val changingInt_v2: Unit = () // not the same program

  // side effects are inevitable
  /* Effect desires
    - the type signature describes what KIND of computation it will perform
    - the type signature describes the type of VALUE it will produce
    - if side effects are required, construction must be separate from the EXECUTION
  */

  /*
    Example 1: Option
    - type signature describes the kind of computation = a possibly absent value
    - the type signature says that computation returns an A if th computation does produce something
    - No side effects are needed

    => Option is an effect
  */
  val anOption: Option[Int] = Option(42)

  /* Example 2: Future
   - describes an asynchronous computation
   - produces a value of type A, if it finishes and it's successful
   - side effects are required, construction is NOT SEPARATE from execution

   => Future is not an effect
  */
  import scala.concurrent.ExecutionContext.Implicits.global

  /*
    Example 3: MyIO
    - describes a computation which might perform side effects
    - produces a value of type A if the computation is successful
    - side effects are required, construction IS SEPARATE from execution

    My IO IS AN EFFECT (the most general one as it can run any effect)
   */
  case class MyIO[A](unsafeRun: () => A) {
    def map[B](f: A => B): MyIO[B] =
      MyIO(() => f(unsafeRun()))

    def flatMap[B](f: A => MyIO[B]): MyIO[B] =
      MyIO(() => f(unsafeRun()).unsafeRun())
  }

  /**
   *
   * Exercises - create some IO which
   * 1. measure the current time of the system
   * 2. measure the duration of a computation
   *  - use exercise 1
   *  - use map/flatMap combinations of MYIO
   * 3 read something from the console
   * 4. print something to hte console (e.g. "what's your name"), then read, then print a welcome message
   *
  */
  // 1
  val currentTime: MyIO[Long] = MyIO(() => System.currentTimeMillis())
  // 2
  def measure[A](computation: MyIO[A]): MyIO[(Long, A)] = for {
      startTime <- currentTime
      result <- computation
      endTime <- currentTime
    } yield (endTime - startTime, result)

  def measure_v2[A](computation: MyIO[A]): MyIO[(Long, A)] = {
    MyIO{ () =>
     val startTime = System.currentTimeMillis()
     val result = computation.unsafeRun()
     val endTime =  System.currentTimeMillis()
     (endTime - startTime, result)
    }
  }


  def measure_v3[A](computation: MyIO[A]): MyIO[(Long, A)] =
    currentTime.flatMap { startTime =>
      computation.flatMap { result =>
        currentTime.map { endTime =>
          (endTime - startTime, result)
        }
      }
    }

  def demoMeasurement(): Unit = {
    val computation = MyIO(() => {
      println("Crunching numbers...")
      Thread.sleep(1000)
      println("Done!")
      42
    })

    println(measure(computation).unsafeRun())
    println(measure_v2(computation).unsafeRun())
  }

  // 3
  val readLine: MyIO[String] = MyIO(() => StdIn.readLine())
  def putStrLn(line: String): MyIO[Unit] = MyIO(() => println(line))

  // 4 WIth IO you write kind of imperative code but describing a sequence of effects
  // as pure programs that will run when the efffects are evaluated
  val program: MyIO[Unit] = for {
    _ <- putStrLn("What's your name?")
    name <- readLine
    _ <- putStrLn(s"Welcome to Rock the JVM, $name!")
  } yield ()

  val anIOWithSideEffects: MyIO[Int] = MyIO(() => {
    println("producing effect")
    42
  })

  case class MyZIO[-R, +E, +A](unsafeRun: R => Either[E, A]) {
    def map[B](f: A => B): MyZIO[R, E, B] =
      MyZIO(r => unsafeRun(r) match {
        case Left(e) => Left(e)
        case Right(v) => Right(f(v))
      })

    def flatMap[R1 <: R, E1 >: E, B](f: A => MyZIO[R1, E1, B]): MyZIO[R1, E1, B] =
      MyZIO(r => unsafeRun(r) match {
        case Left(e) => Left(e)
        case Right(v) => f(v).unsafeRun(r)
      })
  }

  def main(args: Array[String]): Unit = {
    //anIOWithSideEffects.unsafeRun()
    //demoMeasurement()
    program.unsafeRun()
  }
}
