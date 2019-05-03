package flowly.core.context

import flowly.core.variables.Key

trait WritableExecutionContext extends ReadableExecutionContext {
  def set[T](key: Key[T], value: T): Unit
  def unset(key: Key[_]): Unit
}
