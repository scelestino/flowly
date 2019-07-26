package flowly.core.tasks.compose

import java.time.Instant

import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{OnError, TaskResult, ToRetry}
import flowly.core.tasks.strategies.scheduling.SchedulingStrategy
import flowly.core.tasks.strategies.stopretrying.StopRetryingStrategy
import flowly.core.variables.{ExecutionContext, ReadableExecutionContext}

trait RetryableTask extends Task {

  protected def schedulingStrategy: SchedulingStrategy

  protected def stopRetryingStrategy: StopRetryingStrategy

//  protected def onRetryingStop(executionContext: ReadableExecutionContext): Unit = {}

  abstract override private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = {
    super.execute(sessionId, executionContext) match {
      case OnError(cause) => ToRetry(cause, Instant.now) // TODO: parece que hay que cambiar la firma de execute para que tenga mas info
      case result => result
    }

  }

//  private def buildNewAttemptsInfo(executionContext: ExecutionContext, lastError: Throwable): TaskAttempts = {
//    val currentAttemptsInfo = getCurrentAttemptsInfo(executionContext, lastError)
//    if(stopRetryingStrategy.shouldRetry(currentAttemptsInfo, executionContext)){
//      val nextRetryDate = schedulingStrategy.nextRetryDate(currentAttemptsInfo, executionContext)
//      currentAttemptsInfo.withNextRetry(nextRetryDate)
//    } else {
//      onRetryingStop(executionContext)
//      currentAttemptsInfo.stopRetrying()
//    }
//  }

//  private def getCurrentAttemptsInfo(executionContext: ExecutionContext, lastError: Throwable): TaskAttempts = {
//    executionContext.get(TaskAttemptsKey) match {
//      case None => TaskAttempts(lastError)
//      case Some(taskAttempts: TaskAttempts) => taskAttempts.withLastAttempt(lastError)
//    }
//  }

}

