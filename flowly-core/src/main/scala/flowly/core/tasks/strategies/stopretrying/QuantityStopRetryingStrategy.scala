package flowly.core.tasks.strategies.stopretrying
import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class QuantityStopRetryingStrategy(maxAttempts: Int) extends StopRetryingStrategy {

  override def shouldRetry(taskAttempts: TaskAttempts, readableExecutionContext: ReadableExecutionContext): Boolean = {
    taskAttempts.quantity < maxAttempts
  }

}