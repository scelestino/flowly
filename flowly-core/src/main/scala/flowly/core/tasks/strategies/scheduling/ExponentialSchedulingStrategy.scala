package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class ExponentialSchedulingStrategy(secondsForFirstRetry: Int) extends SchedulingStrategy {

  override def nextRetryDate(taskAttempts: TaskAttempts, executionContext: ReadableExecutionContext): Instant = {
    taskAttempts.lastAttempt.plusSeconds(Math.pow(secondsForFirstRetry, taskAttempts.quantity).toLong)
  }

}

object ExponentialSchedulingStrategy {
  def apply(secondsForFirstRetry: Int): ExponentialSchedulingStrategy = new ExponentialSchedulingStrategy(secondsForFirstRetry)
}
