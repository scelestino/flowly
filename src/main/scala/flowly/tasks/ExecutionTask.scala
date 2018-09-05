package flowly.tasks

import flowly.context.ExecutionContext
import flowly.{Continue, Error, TaskResult}

sealed trait PerformResult
case class PerformOk(ctx: ExecutionContext) extends PerformResult
case class PerformError(msg: String) extends PerformResult

/**
  * An instance of this [[Task]] will execute your code and can change the execution context.
  *
  */
trait ExecutionTask extends SingleTask {

  def execute(ctx: ExecutionContext): TaskResult = try {
    perform(ctx) match {
      case PerformOk(updatedCtx) => Continue(id, next, updatedCtx)
      case PerformError(_) => Error(id)
    }
  } catch {
    case e: Throwable => Error(id)
  }

  protected def perform(ctx: ExecutionContext): PerformResult

  protected def ok(ctx: ExecutionContext) = PerformOk(ctx)
  protected def error(msg: String) = PerformError(msg)

}

object ExecutionTask {

  def apply(_id:String, _next:Task)(_perform:ExecutionContext => PerformResult): ExecutionTask = new ExecutionTask {
    def id: String = _id
    def next: Task = _next
    def perform(ctx: ExecutionContext): PerformResult = _perform(ctx)
  }

}

