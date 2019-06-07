package flowly.core.tasks

import flowly.core.ErrorOr
import flowly.core.variables.{Key, ReadableVariables, Variables}

trait ConditionalTask extends SingleTask {

  protected def condition(variables: ReadableVariables): Boolean

  protected def perform(sessionId: String, variables: Variables): ErrorOr[Variables]

  override protected def allowedKeys: List[Key[_]] = List.empty

  final private[flowly] def execute(sessionId: String, variables: Variables): TaskResult = try {
      if(condition(variables)) perform(sessionId, variables).fold(OnError, Continue(next, _))
      else SkipAndContinue(next, variables)
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

}

object ConditionalTask {
  def apply(_id: String, _next: Task, _condition: ReadableVariables => Boolean, _allowedKeys: List[Key[_]])(_perform: (String, Variables) => ErrorOr[Variables]): ConditionalTask = new ConditionalTask {

    def id: String = _id

    def next: Task = _next

    def condition(variables: ReadableVariables): Boolean = _condition(variables)

    def perform(sessionId: String, variables: Variables): ErrorOr[Variables] = _perform(sessionId, variables)

    override protected def allowedKeys: List[Key[_]] = _allowedKeys
  }
}
