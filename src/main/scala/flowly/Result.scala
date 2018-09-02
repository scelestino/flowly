package flowly

trait Result {
  def shouldContinue: Boolean
}

case class Ok() extends Result {
  def shouldContinue: Boolean = true
}

case class Finish() extends Result {
  def shouldContinue: Boolean = false
}

case class Blocked() extends Result {
  def shouldContinue: Boolean = false
}

case class Error() extends Result {
  def shouldContinue: Boolean = false
}
