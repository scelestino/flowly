package flowly

import flowly.context.{ExecutionContext, Key}
import flowly.task._

object Main extends App {

  trait Finish1Component {
    lazy val finish = FinishTask("FINISH 1")
  }

  trait Finish2Component {
    lazy val finish2 = FinishTask("FINISH 2")
  }

  trait BlockingComponent {
    this: Finish1Component =>
    lazy val blocking = BlockingTask("BLOCKING", finish, _ => true)
  }

  trait SecondComponent {
    this: BlockingComponent =>
    lazy val second = ExecutionTask("EXECUTING 2", blocking) { ctx =>
      println(ctx.get(Key1))
      PerformOk(ctx)
    }
  }

  trait DisjunctionComponent {
    this: Finish2Component with SecondComponent =>
    lazy val disjunction: Task = DisjunctionTask("Disjunction", (_.sessionId == "AAA", finish2), (_ => true, second))
  }

  trait FirstComponent {
    this: DisjunctionComponent =>
    lazy val first: Task = ExecutionTask("EXECUTING 1", disjunction) { ctx =>
      PerformOk(ctx.set(Key1, "hello world"))
    }
  }

  object Components extends FirstComponent with SecondComponent with DisjunctionComponent with BlockingComponent with Finish1Component with Finish2Component

  val workflow = new Workflow {
    def name: String = "nombre"

    def version: String = "v1"

    def firstTask: Task = Components.first
  }

  val ctx = new ExecutionContext("BBB", Map.empty)


  val result = workflow.execute(ctx.set(Key1, "asdad"))

  println(result)

  println(Components.first.tasks)

}

case object Key1 extends Key[String]

case object Key2 extends Key[Int]


object Main2 extends App {

  import org.json4s.native.Serialization.write

  implicit val format = org.json4s.DefaultFormats


  val ctx = new ExecutionContext("1", Map.empty).set(Key1, "hola").set(Key2, 123)


  val r = write(ctx.variables)


  //  val r = variables.get(Key1)

  println(r)

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

