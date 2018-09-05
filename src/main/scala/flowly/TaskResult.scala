package flowly

import flowly.context.ExecutionContext
import flowly.task.Task

trait TaskResult {
  def taskId:String
  def status:String
}

case class Continue(taskId:String, nextTask:Task, ctx: ExecutionContext) extends TaskResult {
  def status:String = WFStatus.OK
}

case class Finish(taskId:String) extends TaskResult {
  def status:String = WFStatus.FINISHED
}

case class Blocked(taskId:String) extends TaskResult {
  def status:String = WFStatus.BLOCKED
}

case class Error(taskId:String) extends TaskResult {
  def status:String = WFStatus.ERROR
}



object WFStatus extends Enumeration {
  type WFStatus = Value
  val OK, ERROR, FINISHED, BLOCKED, RUNNING = Value

  implicit def enum2String(value: WFStatus): String = value.toString
}