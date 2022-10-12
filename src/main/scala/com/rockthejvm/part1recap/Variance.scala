package com.rockthejvm.part1recap


/*A year of mental strain on #Scala variance, compressed:

Covariant = retrieves or produces T.
Contravariant = acts on, or consumes T.
*/
object Variance {

  //OOP - substitution
  class Animal
  class Dog(name: String) extends Animal

  //Variance question for List: if Dog <: Animal, then should List[Dog] <: List[Animal]

  // YES - COVARIANT

  val lassie = new Dog("Lassie")
  val hachi = new Dog("Hachi")
  val laika = new Dog("Laika")

  val anAnimal: Animal = lassie
  val someAnimal: List[Animal] = List(lassie, hachi, laika)

  class MyList[+A] // MyList is COVARIANT in A (produces)
  val myAnimalList: MyList[Animal] = new MyList[Dog]

  // NO - then type is INVARIANT (two semigroups are not covariant as different type have no subtype relationship between them
  trait Semigroup[A] {
    def combine(x: A, y: A): A
  }

  // all generics in Java are invariant too (can't do the following)
  //val aJavaList: java.util.ArrayList[Animal] = new java.util.ArrayList[Dog]()

  // HELL NO - CONTRAVARIANCE (consumes)
  trait Vet[-A] {
    def heal(animal: A): Boolean
  }

  // Vet[Animal] is "better" than a Vet[Dog] because it can treat any animal
  // Dog <: Animal, then Vet[Dog >: Vet[Animal]
  val myVet: Vet[Dog] = new Vet[Animal] {
    override def heal(animal: Animal): Boolean = {
      println("Here you go, you are good now...")
      true
    }
  }

  val healingLassie: Boolean = myVet.heal(lassie)

  /*
    Rule of thumg
    - if the type PRODUCES or retrieves values of type A (e.g. lists), ghen the type should be COVARIANT
    - if the type CONSUMES or ACTS ON values of type A (e.g. a vet, serializer), then the type should be CONTRAVARIANT
   */

  /*
   * Variance positions
   */

  /*
  class Cat extends Animal

  class Vet2[-A](val favoriteAnimal: A) //<-- the types of val fields are in COVARIANT position

  val garfield = new Cat
  val theVet: Vet2[Animal] = new Vet2[Animal](garfield)
  val dogVet: Vet2[Dog] = theVet
  val favAnimal: Dog = dogVet.favoriteAnimal // must be a Dog - type conflict!
*/

  // var fields apply only to invariant types

  /*
  class MutableContainer[+A](var contents: A)
  val containerAnimal: MutableContainer[Animal] = new MutableContainer[Dog](new Dog)
  containerAnimal.contents = new Cat // type conflict!
  */

  // types of method arguments are in CONTRAVARIANT position
/*  class MyList2[+A] {
    def add(element: A): MyList[A]

    val animals: MyList2[Animal] = new MyList2[Cat]
    val biggerListOfAnimals: MyList2[Animal] = animals.add(new Dog) //type conflict

  }*/

  // solution WIDEN the type argument
  class MyList2[+A] {
    def add[B >: A](element: B): MyList[B] = ???
  }

  // method returns types are in COVARIANT position
  /*
  class Vet2[-A] {
    def rescueAnimal(): A
  }

  val vet: Vet2[Animal] = new Vet2[Animal] {
    def rescueAnimal: Animal = new Cat
  }

  val lassieVet = Vet2[Dog] = vet
  val rescueDog: Dog = lassieVet.rescueAnimal() // must return a Dog, but it returns a Cat - type conflict!
  */


  def main(args: Array[String]): Unit = {

  }

}
