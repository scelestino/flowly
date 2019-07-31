package flowly.core.tasks.model

import java.time.Instant

import flowly.core.repository.model.Execution

class puchi private (execution:Option[Execution], taskAttempts:Option[TaskAttempts]) {
  def firstAttempt:Instant = taskAttempts.map(_.firstAttempt).getOrElse(Instant.now)
  def lastAttempt:Instant = execution.map(_.at).getOrElse(Instant.now)
  def attempts:Int = taskAttempts.map(_.quantity).getOrElse(1)
}

object puchi {
  def apply(execution: Option[Execution], taskAttempts: Option[TaskAttempts]): puchi = new puchi(execution, taskAttempts)
}