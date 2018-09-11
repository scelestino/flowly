/*
 * Copyright © 2018-2019 the flowly project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flowly

import flowly.tasks._
import flowly.tasks.context.Key

object Main extends App {

  trait Finish1Component {
    lazy val finish = FinishTask("FINISH 1")
  }

  trait Finish2Component {
    lazy val finish2 = FinishTask("FINISH 2")
  }

  trait BlockingComponent {
    this: Finish1Component =>
    lazy val blocking = BlockingTask("BLOCKING", finish, _.contains(Key3))
  }

  trait SecondComponent {
    this: BlockingComponent =>
    lazy val second: Task = ExecutionTask("EXECUTING 2", blocking) { ctx =>
      println(ctx.get(Key1))
      Right(ctx.set(Key2, 1234))
    }
  }

  trait DisjunctionComponent {
    this: Finish2Component with SecondComponent =>
    lazy val disjunction: Task = DisjunctionTask("Disjunction", finish2, second, _.sessionId == "AAA")
  }

  trait FirstComponent {
    this: DisjunctionComponent =>
    lazy val first: Task = ExecutionTask("EXECUTING 1", disjunction) { ctx =>
      Right(ctx.set(Key1, "foo bar baz"))
    }
  }

  object Components extends FirstComponent with SecondComponent with DisjunctionComponent with BlockingComponent with Finish1Component with Finish2Component

  val workflow = new Workflow {
    def initialTask: Task = Components.first
  }


  val result = for {

    sessionId <- workflow.init()

    result <- workflow.execute(sessionId)

    _ = println(s"the result is $result\n")

    result2 <- workflow.execute(sessionId, Param(Key3, false))

  } yield result2


  result match {
    case Right(r) => println(s"THE RESULT CONTAINS ${r.variables}")
    case Left(ex) => println(ex)
  }


}

case object Key1 extends Key[String]

case object Key2 extends Key[Int]

case object Key3 extends Key[Boolean]



object Main2 extends App {

  //  import org.json4s.native.Serialization.write
  //
  //  implicit val format = org.json4s.DefaultFormats
  //
  //
  //  val ctx = new TaskContext("1", Map.empty).set(Key1, "hola").set(Key2, 123)
  //
  //  val r = write(ctx.variables)
  //
  //
  //    val r = variables.get(Key1)
  //
  //  println(r)


  trait Animal {
    def stop(): Unit = println("stop moving")
  }

  class Dog extends Animal {
    def bark: String = "Woof!"
  }

  trait Security {
    this: Animal =>
    def lookout: Unit = {
      stop(); println("looking out!")
    }
  }

  val goodboy: Dog = new Dog

  goodboy.bark
  // Woof!


  val guardDog = new Dog with Security
  guardDog.lookout


  trait Patient {
    this: Reader =>
    def isQuite: Boolean = isReading

    def isSlow: Boolean = true
  }

  trait Reader {
    this: Patient =>
    def read(): Unit = if (isSlow) println("Reading Slow...") else println("Reading Fast...")

    def isReading = true
  }

  val person = new Patient with Reader


  trait Human {
    def isGoodForSports: Boolean
  }

  trait Programmer extends Human {
    def readStackOverflow(): Unit = println("Reading...")

    override def isGoodForSports: Boolean = false
  }

  trait Sportsman extends Human {
    def play(): Unit = println("Playing something")

    override def isGoodForSports: Boolean = true
  }

  val foo = new Programmer with Sportsman
  println(foo.isGoodForSports)

  val bar = new Sportsman with Programmer
  println(bar.isGoodForSports)


}

//object Main2 extends App {
//
//  import org.json4s.scalaz.JsonScalaz._
//  import scalaz._
//  import Scalaz._
//
//  case class Person(name: String)
//
//  object Person {
//
//    implicit val json: JSON[Person] = new JSON[Person] {
//      def write(value: Person): JValue = JString("putos")
//
//      def read(json: JValue): JsonScalaz.Result[Person] = Person("vamo lo pibe").successNel
//    }
//
//  }
//
//
//  private val value: JValue = toJSON(Person("pepito"))
//
//  println(value)
//
//
//  private val value2: JsonScalaz.Result[Person] = fromJSON[Person](value)
//
//  println(value2)
//
//
//}

