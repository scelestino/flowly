package flowly.core

import flowly.core.context.ExecutionContext

trait TasksContext extends Context {
  lazy val variables = Seq[Param](StringKey -> "value1").toVariables
  val ec: ExecutionContext = new ExecutionContext(variables, None, serializer)
}
