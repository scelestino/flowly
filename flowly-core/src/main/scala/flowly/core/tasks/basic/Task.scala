package flowly.core.tasks.basic

import flowly.core.tasks.model.TaskResult
import flowly.core.variables.{ExecutionContext, Key}
import flowly.core.{ErrorOr, Param, ParamsNotAllowed}

/**
  * [[Task]] is something to do inside a workflow
  *
  * There is no possible to use two identical [[Task]] in the same workflow
  *
  */
trait Task {

  val id: String = this.getClass.getSimpleName

  /**
    * Perform a single step inside the workflow. It depends on the task implementation
    */
  private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult

  /**
    * Check if all the keys are allowed by this task
    */
  final private[flowly] def accept(params: List[Param]): ErrorOr[Unit] = {
    if(params.forall{case Param(key, value) => allowedKeys.exists( k => k.identifier == key && k.allowedType(value))}) {
      Right()
    } else {
      Left(ParamsNotAllowed(allowedKeys.map(_.identifier), params))
    }
  }


  /**
    * A list of tasks that follows this task
    */
  private[flowly] def followedBy: List[Task]

  /**
    * A list of keys allowed by this task. It means that a session on this task can be
    * executed with these keys
    */
  def allowedKeys: List[Key[_]]

  override def toString: String = s"Task:$id"

}
