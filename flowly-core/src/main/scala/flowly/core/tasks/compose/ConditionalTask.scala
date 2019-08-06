//package flowly.core.tasks.compose

//import flowly.core.ErrorOr
//import flowly.core.tasks.basic.{SingleTask, Task}
//import flowly.core.tasks.model.{Continue, OnError, SkipAndContinue, TaskResult}
//import flowly.core.variables.{ExecutionContext, Key, ReadableExecutionContext}
//
//trait ConditionalTask extends SingleTask {
//
//  protected def condition(variables: ReadableExecutionContext): Boolean
//
//  protected def perform(sessionId: String, variables: ExecutionContext): ErrorOr[ExecutionContext]
//
//  override def allowedKeys: List[Key[_]] = List.empty
//
//  final private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = try {
//      if(condition(executionContext)) perform(sessionId, executionContext).fold(OnError, Continue(next, _))
//      else SkipAndContinue(next)
//  } catch {
//    case throwable: Throwable => OnError(throwable)
//  }
//
//}
//
//object ConditionalTask {
//  def apply(_id: String, _next: Task, _condition: ReadableExecutionContext => Boolean, _allowedKeys: List[Key[_]])(_perform: (String, ExecutionContext) => ErrorOr[ExecutionContext]): ConditionalTask = new ConditionalTask {
//
//    override val id: String = _id
//
//    val next: Task = _next
//
//    def condition(variables: ReadableExecutionContext): Boolean = _condition(variables)
//
//    def perform(sessionId: String, variables: ExecutionContext): ErrorOr[ExecutionContext] = _perform(sessionId, variables)
//
//    override def allowedKeys: List[Key[_]] = _allowedKeys
//  }
//}
