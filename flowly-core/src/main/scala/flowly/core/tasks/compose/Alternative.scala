package flowly.core.tasks.compose

import flowly.core.context.ExecutionContext
import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{Continue, OnError, TaskResult}

trait Alternative extends Task with Dependencies {

  protected val nextOnError:Task

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {
    super.execute(sessionId, executionContext) match {
      case OnError(_) => Continue(nextOnError, executionContext)
      case otherwise => otherwise
    }
  }

  /**
    * A list of tasks that follows this task
    */
  abstract override private[flowly] def followedBy: List[Task] = nextOnError :: super.followedBy

  /**
    * This method give us a compile-time check about [[Alternative]] use
    */
  override private[flowly] final def alternativeAfterAll():Unit = ()

}