package flowly

import flowly.context.ExecutionContext
import flowly.tasks.Task

trait Workflow {

  def name:String
  def version:String

  def firstTask:Task

  def execute(ctx:ExecutionContext):TaskResult = {

    def execute(task:Task, ctx:ExecutionContext):TaskResult = {

      println(s"Executing... ${task.id}")

      task.execute(ctx) match {
        case Continue(_, next, updatedCtx) => execute(next, updatedCtx)
        case otherwise                     => otherwise
      }

    }
    execute(firstTask, ctx)



  }

}