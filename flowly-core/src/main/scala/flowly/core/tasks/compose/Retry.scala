package flowly.core.tasks.compose

import java.time.Instant

import flowly.core.context.ExecutionContext
import flowly.core.repository.model.Attempts
import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{OnError, TaskResult, ToRetry}
import flowly.core.tasks.strategies.scheduling.SchedulingStrategy
import flowly.core.tasks.strategies.stopping.StoppingStrategy

trait Retry extends Task with Dependencies {

  protected def schedulingStrategy: SchedulingStrategy

  protected def stoppingStrategy: StoppingStrategy

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {
    try {
      val attempts = executionContext.attempts.getOrElse(Attempts(1, Instant.now, None))
      super.execute(sessionId, executionContext) match {
        case OnError(cause) if stoppingStrategy.shouldRetry(executionContext, attempts) =>
          ToRetry(cause, attempts.withNextRetry(schedulingStrategy.nextRetry(executionContext, attempts)))
        case
          otherwise => otherwise
      }
    } catch {
      case throwable: Throwable => OnError(throwable)
    }
  }

  /**
    * This method give us a compile-time check about [[Alternative]] use
    */
  override private[flowly] def alternativeAfterAll():Unit = ()

}