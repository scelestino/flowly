package flowly

import flowly.context._
import org.json4s.{CustomSerializer, JValue}
import org.json4s.JsonAST.JString
import org.json4s.scalaz.JsonScalaz


trait Task {

  def id: String

  def path(ctx: ExecutionContext): Stream[Task]

  def perform(ctx: ExecutionContext): Result

  private def tasks(previous: List[Task]): List[Task] = {
    if (previous.contains(this)) previous
    else followedBy.foldLeft(this :: previous) {
      (seed, task) => task.tasks(seed)
    }
  }
  def tasks: List[Task] = tasks(Nil)

  def followedBy: List[Task]

  override def toString: String = s"Task:$id"

}

trait BaseTask extends Task {

  def next: Task

  def path(ctx: ExecutionContext): Stream[Task] = this #:: next.path(ctx)

  def followedBy: List[Task] = next :: Nil

}

trait DisjunctionTask extends Task {

  def branches: List[(ReadableExecutionContext => Boolean, Task)]

  def path(ctx: ExecutionContext): Stream[Task] = next(ctx) match {
    case Some(task) => this #:: task.path(ctx)
    case None => this #:: Stream.empty[Task]
  }

  def perform(ctx: ExecutionContext): Result = {
    next(ctx) match {
      case Some(task) => Ok()
      case None => Error()
    }
  }

  def followedBy: List[Task] = branches.collect { case (_, task) => task }

  private def next(ctx: ExecutionContext): Option[Task] = branches.collectFirst { case (condition, task) if condition(ctx) => task }

}

trait FinishTask extends Task {

  def path(ctx: ExecutionContext): Stream[Task] = Stream(this)

  def perform(ctx: ExecutionContext): Result = {
    println(s"FINISH! $id")
    Finish()
  }

  def followedBy: List[Task] = Nil

}

trait BlockingTask extends BaseTask {

  def condition(ctx: ReadableExecutionContext): Boolean

  def perform(ctx: ExecutionContext): Result = if (condition(ctx)) Ok() else Blocked()

}

trait ExecutionTask extends BaseTask {

  def perform(ctx: ExecutionContext): Result = try {
    execute(ctx) match {
      case ExecutionOk => Ok()
      case ExecutionError => Error()
    }
  } catch {
    case e: Throwable => Error()
  }

  protected def execute(ctx: WriteableExecutionContext): ExecutionResult

  trait ExecutionResult

  object ExecutionOk extends ExecutionResult

  object ExecutionError extends ExecutionResult

}


object Main extends App {

  trait Finish1Component {
    val finish:Task = new FinishTask {
      def id: String = "3"
    }
  }

  trait Finish2Component {
    val finish2:Task = new FinishTask {
      def id: String = "4"

      def perform: Result = {
        println("FINISH2!!!")
        Finish()
      }
    }
  }

  trait BlockingComponent {
    this: Finish1Component =>
    val blocking:Task = new BlockingTask {

      def condition(ctx: ReadableExecutionContext): Boolean = true

      def next: Task = finish

      def id: String = "BLOCKING!"

    }
  }

  trait SecondComponent {
    this: BlockingComponent =>
    val second:Task = new ExecutionTask {

      def next: Task = blocking

      def id: String = "2"

      def execute(ctx: WriteableExecutionContext): ExecutionResult = {
        println("executing 2")
        println(ctx.get(Key1))
        ExecutionOk
      }

    }
  }

  trait ConditionalComponent {
    this: Finish2Component with SecondComponent =>
    val conditional:Task = new DisjunctionTask {

      def branches: List[(ReadableExecutionContext => Boolean, Task)] = ((ctx: ReadableExecutionContext) => ctx.sessionId == "AAA", finish2) :: ((ctx: ReadableExecutionContext) => true, second) :: Nil

      def id: String = "disjunction"

    }
  }

  trait FirstComponent {
    this: ConditionalComponent =>
    val first:Task = new ExecutionTask {

      def next: Task = conditional

      def id: String = "1"

      def execute(ctx: WriteableExecutionContext): ExecutionResult = {
        println("this is the 1")
        ExecutionOk
      }

    }
  }

  object Components extends FirstComponent with SecondComponent with ConditionalComponent with BlockingComponent with Finish1Component with Finish2Component

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

  import org.json4s.native.JsonMethods._
  import org.json4s.native.Serialization.{read, write}

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

