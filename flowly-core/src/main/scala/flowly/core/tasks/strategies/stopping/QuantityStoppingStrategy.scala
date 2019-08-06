package flowly.core.tasks.strategies.stopping
import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts
import flowly.core.tasks.compose.StoppingStrategy

class QuantityStoppingStrategy(maxAttempts: Int) extends StoppingStrategy {

  def shouldRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Boolean = {
    attempts.quantity < maxAttempts
  }

}