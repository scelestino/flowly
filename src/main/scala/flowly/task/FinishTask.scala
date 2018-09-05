package flowly.task

import flowly.context.ExecutionContext
import flowly.{Finish, TaskResult}

case class FinishTask(id:String) extends Task {

  def execute(ctx: ExecutionContext): TaskResult = Finish(id)

  def followedBy: Seq[Task] = Nil

}