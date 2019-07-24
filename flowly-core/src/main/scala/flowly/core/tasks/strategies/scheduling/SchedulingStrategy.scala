package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

trait SchedulingStrategy {

  def nextRetryDate(taskAttempts: TaskAttempts, executionContext: ReadableExecutionContext): Instant

}
