package flowly.core.tasks.basic

import flowly.core.tasks.model.TaskResult
import flowly.core.context.{ExecutionContext, Key}
import flowly.core.{ErrorOr, Param, ParamsNotAllowed}

/**
  * [[Task]] is something to do inside a workflow
  *
  * There is no possible to use two identical [[Task]] in the same workflow
  *
  */
trait Task {

//  def id: String = this.getClass.getSimpleName

  def name:String = this.getClass.getSimpleName

  /**
    * Perform a single step inside the workflow. It depends on the task implementation
    */
  private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult

  /**
    * A list of tasks that follows this task
    */
  private[flowly] def followedBy: List[Task]

  /**
    * Check if all the keys are allowed by this task
    */
  def accept(keys: List[Key[_]]): Boolean = keys.forall(allowedKeys.contains)

  /**
    * A list of keys allowed by this task. It means that a session on this task can be
    * executed with these keys
    */
  final def allowedKeys: List[Key[_]] = customAllowedKeys ++ internalAllowedKeys

  private[flowly] def internalAllowedKeys: List[Key[_]] = Nil

  protected def customAllowedKeys: List[Key[_]]

  override def toString: String = s"Task:$name"

}
