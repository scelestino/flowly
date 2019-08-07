package flowly.core.tasks.strategies.stopping

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts

trait StoppingStrategy {
  def shouldRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Boolean
}
