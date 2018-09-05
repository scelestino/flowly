package flowly.tasks

import flowly.context.{ExecutionContext, ReadableExecutionContext}
import flowly.{Error, Continue, TaskResult}

/**
  * An instance of this [[Task]] will choose a branch of execution between different paths based on given conditions.
  *
  * It will test each condition until find any that works. If no condition works, this [[Task]] will fail.
  *
  */
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

  def apply(_id:String, ifTrue:Task, ifFalse:Task, condition: ReadableExecutionContext => Boolean): DisjunctionTask = {
    apply(_id, (condition, ifTrue), (_ => true, ifFalse) )
  }

}