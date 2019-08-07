package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts

import scala.annotation.tailrec

class FibonacciSchedulingStrategy(multiplier: Int, upperBound: Int) extends SchedulingStrategy {

  def nextRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Instant = {
    Instant.now.plusSeconds( upperBound.min( multiplier * fibonacci(attempts.quantity) ))
  }

  private def fibonacci(i: Int): Int = {
    @tailrec
    def fibTail(i: Int, a: Int, b: Int): Int = i match {
      case 0 => a
      case _ => fibTail( i - 1, b, a + b )
    }
    fibTail(i, 0, 1)
  }

}
