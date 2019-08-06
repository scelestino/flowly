package flowly.core.tasks.strategies.stopping
import java.time.Instant
import java.time.temporal.ChronoUnit

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts
import flowly.core.tasks.compose.StoppingStrategy

class TimeBasedStoppingStrategy(maxMinutesToRetry: Int) extends StoppingStrategy {

  def shouldRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Boolean = {
    ChronoUnit.MINUTES.between(attempts.firstAttempt, Instant.now) < maxMinutesToRetry
  }

}