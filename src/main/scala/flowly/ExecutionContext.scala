package flowly

class ExecutionContext(val sessionId:String, variables: Map[String, Any]) {

  def get[T](key:String):Option[T] = variables.get(key).asInstanceOf[Option[T]]

  def getOrElse[T](key:String, f: => T):T = get(key).getOrElse(f)

  def set[T](key:String, value:T):ExecutionContext = new ExecutionContext(sessionId, variables.updated(key, value))

  def unset(key:String):ExecutionContext = new ExecutionContext(sessionId, variables.filterKeys(_ != key))

  def contains(key:String):Boolean = variables.contains(key)

  def exists[T](key:String, f:T => Boolean):Boolean = get(key).exists(f)

}
