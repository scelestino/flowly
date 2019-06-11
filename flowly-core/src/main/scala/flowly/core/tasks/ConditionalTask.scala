package flowly.core.tasks

import flowly.core.ErrorOr
import flowly.core.variables.{Key, ReadableExecutionContext, ExecutionContext}

trait ConditionalTask extends SingleTask {

  protected def condition(variables: ReadableExecutionContext): Boolean

  protected def perform(sessionId: String, variables: ExecutionContext): ErrorOr[ExecutionContext]

  override def allowedKeys: List[Key[_]] = List.empty

  final private[flowly] def execute(sessionId: String, variables: ExecutionContext): TaskResult = try {
      if(condition(variables)) perform(sessionId, variables).fold(OnError, Continue(next, _))
      else SkipAndContinue(next, variables)
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

}

object ConditionalTask {
  def apply(_id: String, _next: Task, _condition: ReadableExecutionContext => Boolean, _allowedKeys: List[Key[_]])(_perform: (String, ExecutionContext) => ErrorOr[ExecutionContext]): ConditionalTask = new ConditionalTask {

    def id: String = _id

    def next: Task = _next

    def condition(variables: ReadableExecutionContext): Boolean = _condition(variables)

    def perform(sessionId: String, variables: ExecutionContext): ErrorOr[ExecutionContext] = _perform(sessionId, variables)

    override def allowedKeys: List[Key[_]] = _allowedKeys
  }
}
