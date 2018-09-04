package flowly.context

/**
  * Interface used to get access to objects inside an [[ExecutionContext]], it creates a relation
  * between an identifier and a type.
  *
  * {{{
  *
  *   case object Key1 extends Key[String]
  *   case object Key2 extends Key[Int]
  *   case object Key3 extends Key[List[Foo]]
  *
  *   val ctx = new ExecutionContext("bar", Map.empty).set(Key2, 123)
  *
  * }}}
  *
  * @tparam T what kind of objects can be used with this key
  */
trait Key[T] {
  this: Product =>
  def identifier:String = this.toString
}