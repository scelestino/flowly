package flowly.core.tasks

import flowly.core.ErrorOr
import flowly.core.context.{Key, ReadableExecutionContext, WritableExecutionContext}
import flowly.core.tasks.basic.Task


object BlockingTask {

  def apply(_id: String, _next: Task, _condition: ReadableExecutionContext => Boolean, _allowedKeys: List[Key[_]]): basic.BlockingTask = new basic.BlockingTask {

    override def id: String = _id

    val next: Task = _next

    def condition(variables: ReadableExecutionContext): Boolean = _condition(variables)

    override protected def customAllowedKeys = Nil

  }

}

object DisjunctionTask {

  def apply(_id: String, _branches: (ReadableExecutionContext => Boolean, Task)*): basic.DisjunctionTask = new basic.DisjunctionTask {

    override def id: String = _id

    override protected def customAllowedKeys = Nil

    def branches: List[(ReadableExecutionContext => Boolean, Task)] = _branches.toList
  }

  def apply(_id: String, ifTrue: Task, ifFalse: Task, condition: ReadableExecutionContext => Boolean): basic.DisjunctionTask = {
    apply(_id, (condition, ifTrue), (_ => true, ifFalse))
  }

}

object BlockingDisjunctionTask {
  def apply(_id: String, _allowedKeys: List[Key[_]], _branches: (ReadableExecutionContext => Boolean, Task)*): basic.DisjunctionTask = new basic.DisjunctionTask {

    override def id: String = _id

    override protected def customAllowedKeys = Nil

    def branches: List[(ReadableExecutionContext => Boolean, Task)] = _branches.toList

    override protected def blockOnNoCondition = true

  }
}

object ExecutionTask {

  def apply(_id: String, _next: Task)(_perform: (String, WritableExecutionContext) => ErrorOr[WritableExecutionContext]): basic.ExecutionTask = new basic.ExecutionTask {

    override def id: String = _id

    val next: Task = _next

    def perform(sessionId: String, executionContext: WritableExecutionContext): ErrorOr[WritableExecutionContext] = _perform(sessionId, executionContext)

  }

}