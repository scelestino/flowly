package flowly.core.tasks.compose

import java.time.Instant

import flowly.core.repository.model.Attempts
import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{OnError, TaskResult, ToRetry}
import flowly.core.context.{ExecutionContext, ReadableExecutionContext}

trait Retry extends Task {

  protected def schedulingStrategy: SchedulingStrategy

  protected def stoppingStrategy: StoppingStrategy

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {

    val attempts = executionContext.attempts.getOrElse(Attempts(1, Instant.now, None))

    super.execute(sessionId, executionContext) match {
      case OnError(cause) if stoppingStrategy.shouldRetry(executionContext, attempts) =>
        ToRetry(cause, attempts.withNextRetry(schedulingStrategy.nextRetry(executionContext, attempts)))
      case
        otherwise => otherwise
    }

  }

}

trait StoppingStrategy {
  def shouldRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Boolean
}

trait SchedulingStrategy {
  def nextRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Instant
}