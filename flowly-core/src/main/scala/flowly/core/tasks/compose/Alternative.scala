package flowly.core.tasks.compose

import flowly.core.context.ExecutionContext
import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{Continue, OnError, TaskResult}

trait Alternative extends Task {

  val nextOnError:Task

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {
    super.execute(sessionId, executionContext) match {
      case OnError(_) => Continue(nextOnError, executionContext)
      case otherwise => otherwise
    }
  }

}