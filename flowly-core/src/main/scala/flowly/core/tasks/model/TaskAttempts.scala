package flowly.core.tasks.model

import java.time.Instant

case class TaskAttempts(quantity: Int, firstAttempt: Instant, nextRetry: Option[Instant]) {

  def stopRetrying(): TaskAttempts = copy(nextRetry = None)

  def newAttempt(): TaskAttempts = copy(quantity = quantity + 1)

  def withNextRetry(nextRetry: Instant): TaskAttempts = copy(nextRetry = Option(nextRetry))

}

object TaskAttempts {
  def apply(): TaskAttempts = TaskAttempts(quantity = 0, firstAttempt = Instant.now(), None)
}
