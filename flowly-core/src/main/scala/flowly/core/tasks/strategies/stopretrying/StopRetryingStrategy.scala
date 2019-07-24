package flowly.core.tasks.strategies.stopretrying

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

trait StopRetryingStrategy {

  def shouldRetry(taskAttempts: TaskAttempts, readableExecutionContext: ReadableExecutionContext): Boolean

}