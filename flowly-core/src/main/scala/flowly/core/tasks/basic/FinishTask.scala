package flowly.core.tasks.basic

import flowly.core.tasks.model.{Finish, TaskResult}
import flowly.core.context.{ExecutionContext, Key}

/**
  * An instance of this [[Task]] is need be to used to finish a workflow execution.
  *
  * It is possible to configure multiple [[FinishTask]] inside the same workflow.
  *
  * Once an execution reach this kind of [[Task]], the workflow instance where it is used will finish.
  *
  * @param id task id
  */
case class FinishTask(override val id: String) extends Task {

  def allowedKeys: List[Key[_]] = Nil

  private[flowly] def execute(sessionId: String, variables: ExecutionContext): TaskResult = Finish

  private[flowly] def followedBy: List[Task] = Nil

}
