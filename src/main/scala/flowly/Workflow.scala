package flowly

import flowly.context.ExecutionContext

trait Workflow {

  def name:String
  def version:String

  def firstTask:Task

  def execute(ctx:ExecutionContext) = {
    firstTask.path(ctx).map { task =>
      task.perform(ctx)
    }.find(!_.shouldContinue)
  }

}