package flowly.task

import flowly.context.{ExecutionContext, ReadableExecutionContext}
import flowly.{Blocked, Continue, TaskResult}

trait BlockingTask extends SimpleTask {

  def condition(ctx:ReadableExecutionContext): Boolean

  def execute(ctx: ExecutionContext): TaskResult = if (condition(ctx)) Continue(id, next, ctx) else Blocked(id)

}

object BlockingTask {
  def apply(_id:String, _next:Task, _condition: ReadableExecutionContext => Boolean): BlockingTask = new BlockingTask {
    def id: String = _id
    def next: Task = _next
    def condition(ctx: ReadableExecutionContext): Boolean = _condition(ctx)
  }
}