package flowly

/**
  * Interface used by [[BlockingTask]] and [[DisjunctionTask]]
  */
trait ReadableExecutionContext {

  def sessionId:String

  def get[T](key:String):Option[T]

  def getOrElse[T](key:String, f: => T):T

  def contains(key:String):Boolean

  def exists[T](key:String, f:T => Boolean):Boolean

}

/**
  * Interface used by [[ExecutionTask]]
  */
trait WriteableExecutionContext extends ReadableExecutionContext {

  def set[T](key:String, value:T):WriteableExecutionContext

  def unset(key:String):WriteableExecutionContext

}

/**
  * Immutable context that is pass through the tasks during a workflow execution.
  * It can be used from a [[BlockingTask]], [[DisjunctionTask]] or [[ExecutionTask]] in order to make decisions or
  * it can be modified (through a copy) from a [[ExecutionTask]].
  *
  * After a successful task this context is saved in the repository in order to keep safe any modification.
  *
  * @param sessionId workflow instance id
  * @param variables variables stored in the workflow
  */
class ExecutionContext(val sessionId:String, variables: Map[String, Any]) extends WriteableExecutionContext {

  def get[T](key:String):Option[T] = variables.get(key).asInstanceOf[Option[T]]

  def getOrElse[T](key:String, f: => T):T = get(key).getOrElse(f)

  def set[T](key:String, value:T):ExecutionContext = new ExecutionContext(sessionId, variables.updated(key, value))

  def unset(key:String):ExecutionContext = new ExecutionContext(sessionId, variables.filterKeys(_ != key))

  def contains(key:String):Boolean = variables.contains(key)

  def exists[T](key:String, f:T => Boolean):Boolean = get(key).exists(f)

}