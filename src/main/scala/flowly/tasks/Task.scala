package flowly.tasks

import flowly._
import flowly.context._

/**
  * [[Task]] is something to do inside a workflow
  *
  * There is no possible to use two identical [[Task]] in the same workflow
  *
  */
trait Task {

  def id: String

  def execute(ctx: ExecutionContext): TaskResult

  private def tasks(previous: List[Task]): List[Task] = {
    if (previous.contains(this)) previous
    else followedBy.foldLeft(this :: previous) {
      (seed, task) => task.tasks(seed)
    }
  }
  def tasks: List[Task] = {
    tasks(Nil)
  }

  def followedBy: Seq[Task]

  override def toString: String = s"Task:$id"

}