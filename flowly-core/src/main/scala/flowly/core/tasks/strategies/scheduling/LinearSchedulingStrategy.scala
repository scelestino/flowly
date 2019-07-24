package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class LinearSchedulingStrategy(secondsForFirstRetry: Int, multiplier: Int = 1) extends SchedulingStrategy {

  override def nextRetryDate(taskAttempts: TaskAttempts, executionContext: ReadableExecutionContext): Instant = {
    taskAttempts.lastAttempt.plusSeconds(secondsForFirstRetry * multiplier * taskAttempts.quantity)
  }

}

object LinearSchedulingStrategy {
  def apply(secondsForFirstRetry: Int, multiplier: Int = 1): LinearSchedulingStrategy = new LinearSchedulingStrategy(secondsForFirstRetry, multiplier)
}