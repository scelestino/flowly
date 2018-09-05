package flowly.tasks

import flowly.context.ExecutionContext
import flowly.{Finish, TaskResult}

/**
  * An instance of this [[Task]] is need be to used to finish a workflow execution.
  *
  * It is possible to configure multiple [[FinishTask]] inside the same workflow.
  *
  * Once an execution reach this kind of [[Task]], the workflow instance where it is used will finish.
  *
  * @param id task id
  */
case class FinishTask(id:String) extends Task {

  def execute(ctx: ExecutionContext): TaskResult = Finish(id)

  def followedBy: Seq[Task] = Nil

}