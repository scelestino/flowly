package flowly.core

import flowly.core.variables.ExecutionContext

trait TasksContext extends Context {
  lazy val variables = Seq[Param](StringKey -> "value1").toVariables
  val ec: ExecutionContext = new ExecutionContext("session1", variables, serializer)
}
