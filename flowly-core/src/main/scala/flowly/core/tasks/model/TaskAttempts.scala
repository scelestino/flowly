package flowly.core.tasks.model

import java.time.Instant

case class TaskAttempts(quantity: Int,
                        firstAttempt: Instant,
                        lastAttempt: Instant,
                        lastError: Throwable,
                        nextRetry: Option[Instant]) {

  def shouldRetry: Boolean = nextRetry.nonEmpty

  def stopRetrying: TaskAttempts = this.copy(nextRetry = None)

  def setLastAttempt(lastError: Throwable): TaskAttempts = copy(quantity = quantity + 1, lastAttempt = Instant.now(), lastError = lastError)

  def setNextRetry(nextRetry: Instant): TaskAttempts = copy(nextRetry = Option(nextRetry))

}

object TaskAttempts {
  def apply(lastError: Throwable): TaskAttempts = {
    val now = Instant.now()
    new TaskAttempts(quantity = 1, firstAttempt = now, now, lastError, None)
  }
}
