package flowly.core.tasks.compose

import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{OnError, TaskAttempts, TaskAttemptsKey, TaskResult}
import flowly.core.tasks.strategies.scheduling.SchedulingStrategy
import flowly.core.tasks.strategies.stopretrying.StopRetryingStrategy
import flowly.core.variables.{ExecutionContext, ReadableExecutionContext}

trait RetryableTask extends Task {

  protected def schedulingStrategy: SchedulingStrategy

  protected def stopRetryingStrategy: StopRetryingStrategy

  protected def onRetryingStop(executionContext: ReadableExecutionContext): Unit = {}

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {
    super.execute(sessionId, executionContext) match {
      case OnError(cause, _) => OnError(cause, Option(buildNewAttemptsInfo(executionContext, cause)))
      case result => result
    }
  }

  private def buildNewAttemptsInfo(executionContext: ExecutionContext, lastError: Throwable): TaskAttempts = {
    val currentAttemptsInfo = getCurrentAttemptsInfo(executionContext, lastError)
    if(stopRetryingStrategy.shouldRetry(currentAttemptsInfo, executionContext)){
      val nextRetryDate = schedulingStrategy.nextRetryDate(currentAttemptsInfo, executionContext)
      currentAttemptsInfo.setNextRetry(nextRetryDate)
    } else {
      onRetryingStop(executionContext)
      currentAttemptsInfo.stopRetrying
    }
  }

  private def getCurrentAttemptsInfo(executionContext: ExecutionContext, lastError: Throwable): TaskAttempts = {
    executionContext.get(TaskAttemptsKey) match {
      case None => TaskAttempts(lastError)
      case Some(taskAttempts: TaskAttempts) => taskAttempts.setLastAttempt(lastError)
    }
  }

}

