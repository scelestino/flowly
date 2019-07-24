package flowly.core.tasks.strategies.stopretrying
import java.time.Instant
import java.time.temporal.ChronoUnit

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class TimeBasedStopRetryingStrategy(maxMinutesToRetry: Int) extends StopRetryingStrategy {

  override def shouldRetry(taskAttempts: TaskAttempts, readableExecutionContext: ReadableExecutionContext): Boolean = {
    ChronoUnit.MINUTES.between(taskAttempts.firstAttempt, Instant.now()) > maxMinutesToRetry
  }

}