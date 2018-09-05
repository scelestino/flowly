package flowly.task

import flowly.context.{ExecutionContext, ReadableExecutionContext}
import flowly.{Error, Continue, TaskResult}

trait DisjunctionTask extends Task {

  def branches: Seq[(ReadableExecutionContext => Boolean, Task)]

  def next(ctx: ExecutionContext): Option[Task] = branches.collectFirst { case (condition, task) if condition(ctx) => task }

  def execute(ctx: ExecutionContext): TaskResult = {
    next(ctx) match {
      case Some(next) => Continue(id, next, ctx)
      case None => Error(id)
    }
  }

  def followedBy: Seq[Task] = branches.collect { case (_, task) => task }

}

object DisjunctionTask {

  def apply(_id:String, _branches: (ReadableExecutionContext => Boolean, Task)*): DisjunctionTask = new DisjunctionTask {
    def id: String = _id
    def branches: Seq[(ReadableExecutionContext => Boolean, Task)] = _branches
  }

}