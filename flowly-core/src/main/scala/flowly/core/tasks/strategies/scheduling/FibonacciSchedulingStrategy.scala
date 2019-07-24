package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class FibonacciSchedulingStrategy(secondsForFirstRetry: Int) extends SchedulingStrategy {

  override def nextRetryDate(taskAttempts: TaskAttempts, executionContext: ReadableExecutionContext): Instant = {
    val multiplier = getFibonacciNumber(taskAttempts.quantity)
    taskAttempts.lastAttempt.plusSeconds(multiplier * secondsForFirstRetry)
  }

  private def getFibonacciNumber(i: Int): Int = {
    def fibTail(i: Int, a: Int, b: Int): Int = i match {
      case 0 => a
      case _ => fibTail( i - 1, b, a + b )
    }
    fibTail(i, 0, 1)
  }

}

object FibonacciSchedulingStrategy {
  def apply(secondsForFirstRetry: Int): FibonacciSchedulingStrategy = new FibonacciSchedulingStrategy(secondsForFirstRetry)
}
