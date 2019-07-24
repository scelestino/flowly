package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class ConstantSchedulingStrategy(secondsBetweenRetries: Int) extends SchedulingStrategy {

  override def nextRetryDate(taskAttempts: TaskAttempts, executionContext: ReadableExecutionContext): Instant = {
    taskAttempts.lastAttempt.plusSeconds(secondsBetweenRetries)
  }

}

object ConstantSchedulingStrategy {
  def apply(secondsBetweenRetries: Int): ConstantSchedulingStrategy = new ConstantSchedulingStrategy(secondsBetweenRetries)
}
