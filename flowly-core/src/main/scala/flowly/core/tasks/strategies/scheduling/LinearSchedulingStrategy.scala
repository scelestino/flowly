package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts

class LinearSchedulingStrategy(secondsToRetry: Int, multiplier: Int, upperBound: Int) extends SchedulingStrategy {

  def nextRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Instant = {
    Instant.now.plusSeconds( upperBound.min(secondsToRetry * multiplier * attempts.quantity) )
  }

}