package flowly

import scala.collection.immutable.Stream.cons


trait Task {

  def id:String

  def path(ctx:ExecutionContext):Stream[Task]

  def perform(ctx:ExecutionContext):Result

}

trait BaseTask extends Task {

  def next:Task

  def path(ctx:ExecutionContext): Stream[Task] = this #:: next.path(ctx)

}

trait DisjunctionTask extends Task {

  def branches: List[(ExecutionContext => Boolean, Task)]

  def path(ctx:ExecutionContext): Stream[Task] = next(ctx) match {
    case Some(task) => this #:: task.path(ctx)
    case None       => this #:: Stream.empty[Task]
  }

  def perform(ctx:ExecutionContext): Result = {
    next(ctx) match {
      case Some(task) => Ok()
      case None       => Error()
    }
  }

  private def next(ctx:ExecutionContext):Option[Task] = branches.collectFirst { case (condition, task) if condition(ctx) => task }

}

trait BlockingTask extends BaseTask {

  def condition(ctx:ExecutionContext):Boolean

  def perform(ctx:ExecutionContext): Result = if(condition(ctx)) Ok() else Blocked()

}

trait ExecutionTask extends BaseTask {

  def perform(ctx:ExecutionContext): Result = try {
    execute(ctx) match {
      case ExecutionOk    => Ok()
      case ExecutionError => Error()
    }
  } catch {
    case e:Throwable => Error()
  }

  protected def execute(ctx:ExecutionContext):ExecutionResult

  trait ExecutionResult
  object ExecutionOk extends ExecutionResult
  object ExecutionError extends ExecutionResult

}

trait FinishTask extends Task {

  def path(ctx:ExecutionContext): Stream[Task] = Stream(this)

  def perform(ctx: ExecutionContext): Result = {
    println(s"FINISH! $id")
    Finish()
  }

}



case class ExecutionContext(sessionId:String)



object Main extends App {

  val finish = new FinishTask {
    def id: String = "3"
  }

  val finish2 = new FinishTask {
    def id: String = "4"
    def perform: Result = {
      println("FINISH2!!!")
      Finish()
    }
  }

  val blocking = new BlockingTask {

    def condition(ctx:ExecutionContext):Boolean = true

    def next: Task = finish

    def id: String = "BLOCKING!"

  }

  val second = new ExecutionTask {

    def next: Task = blocking

    def id: String = "2"

    def execute(ctx:ExecutionContext): ExecutionResult = {
      println("executing 2")
      ExecutionOk
    }

  }

  val conditional = new DisjunctionTask {

    def branches: List[(ExecutionContext => Boolean, Task)] = ((ctx:ExecutionContext) => ctx.sessionId == "AAA", finish2) :: ((ctx:ExecutionContext) => true, second) :: Nil

    def id: String = "disjunction"

  }

  val first = new ExecutionTask {

    def next: Task = conditional

    def id: String = "1"

    def execute(ctx:ExecutionContext): ExecutionResult = {
      println("this is the 1")
      ExecutionOk
    }

  }

  val ctx = ExecutionContext("BBB")

  def execute(task:Task, ctx:ExecutionContext) = {
    first.path(ctx).map { task =>
      task.perform(ctx)
    }.find(!_.shouldContinue)
  }

  val result = execute(first, ctx)

  println(result)




  implicit class RichStream[A](stream: Stream[A]) {

    /**
      * Similar to takeWhile but it includes the first element that doesn't satisfy the condition
      */
    def takeUntilNot(p: A => Boolean): Stream[A] =
      if(stream.isEmpty) Stream.Empty
      else if(p(stream.head)) cons(stream.head, stream.tail takeUntilNot p)
      else cons(stream.head, Stream.Empty)

  }


}

