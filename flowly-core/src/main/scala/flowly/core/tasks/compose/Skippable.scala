package flowly.core.tasks.compose

import flowly.core.context.{ExecutionContext, Key}
import flowly.core.tasks.basic.{ExecutionTask, Task}
import flowly.core.tasks.model.{SkipAndContinue, TaskResult}

trait Skippable extends Task {
  this: ExecutionTask =>

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {
    if(executionContext.get(Skip).exists(identity)) SkipAndContinue(next, executionContext.unset(Skip)) else super.execute(sessionId, executionContext)
  }

  override private[flowly] def internalAllowedKeys:List[Key[_]] = Skip :: Nil

}

case object Skip extends Key[Boolean]
