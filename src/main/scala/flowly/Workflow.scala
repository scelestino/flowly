/*
 * Copyright © 2018-2019 the flowly project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flowly

import flowly.context.{ExecutionContext, Key}
import flowly.tasks.{Continue, Task, TaskResult}


trait Workflow {

  def initialTask: Task

  // temp
  val repository = new Repository

  def execute(sessionId: String, params:Param*): TaskResult = {

    val session = repository.getSession(sessionId).get // check opt or change to either

    // can be resumed?

    val taskId = session.lastExecution.map(_.taskId).getOrElse(initialTask.id)

    val task = tasks.find(_.id == taskId).get // check opt or change to either

    val newVariables = params.map(_.value).toMap

    def execute(task: Task, ctx: ExecutionContext): TaskResult = {

      // save session runnning

      // try catch??
      task.execute(ctx) match {

        case Continue(_, next, updatedCtx) => {

          // log?
          println(s"${task.id} finished")

          // save session?
          repository.saveSession(session.copy(variables = updatedCtx.variables))

          // execute next
          execute(next, updatedCtx)

        }

        case otherwise => /* log and save? */ otherwise

      }

    }

    execute(task, ExecutionContext(sessionId, session.variables ++ newVariables))

  }

  /**
    * It returns a list of every [[Task]] in this workflow
    *
    * @return list of [[Task]]
    */
  def tasks: List[Task] = {
    def tasks(currentTask: Task, accum: List[Task]): List[Task] = {
      if (accum.contains(currentTask)) accum
      else currentTask.followedBy.foldLeft(currentTask :: accum) {
        (seed, task) => tasks(task, seed)
      }
    }

    tasks(initialTask, Nil)
  }

  // draft
  def variables(sessionId:String):Map[String, Any] = repository.getSession(sessionId).map(_.variables).getOrElse(Map.empty)

}