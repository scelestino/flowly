package flowly.core.context

import flowly.core.variables.Key

trait ReadableExecutionContext {
  def sessionId: String
  def get[T](key: Key[T]): Option[T]
  def contains(key: Key[_]): Boolean
}
