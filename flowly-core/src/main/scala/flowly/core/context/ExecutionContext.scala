package flowly.core.context
import flowly.core.serialization.Serializer
import flowly.core.variables.{Key, Variables}

class ExecutionContext(val sessionId: String, vars: Variables, serializer: Serializer) extends ReadableExecutionContext with WritableExecutionContext {

  override def get[T](key: Key[T]): Option[T] = ???

  override def contains(key: Key[_]): Boolean = ???

  override def set[T](key: Key[T], value: T): Unit = ???

  override def unset(key: Key[_]): Unit = ???
}
