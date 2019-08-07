package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts

trait SchedulingStrategy {
  def nextRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Instant
}
