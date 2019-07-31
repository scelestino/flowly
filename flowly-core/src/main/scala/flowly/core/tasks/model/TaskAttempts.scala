package flowly.core.tasks.model

import java.time.Instant

case class TaskAttempts(quantity: Int, firstAttempt: Instant, nextRetry: Option[Instant]) {

  def newAttempt():TaskAttempts = copy(quantity = quantity + 1)

  def stopRetrying(): TaskAttempts = copy(nextRetry = None)

  def withNextRetry(nextRetry: Instant): TaskAttempts = copy(nextRetry = Option(nextRetry))

}

object TaskAttempts {
  def apply(): TaskAttempts = TaskAttempts(quantity = 1, firstAttempt = Instant.now(), None)
}
