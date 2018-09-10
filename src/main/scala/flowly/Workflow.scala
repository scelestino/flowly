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

import flowly.context.ExecutionContext
import flowly.session.Session
import flowly.tasks._


trait Workflow {

  def initialTask: Task

  // temp
  val repository = new Repository

  def init(params: Param*): ErrorOr[String] = {
    repository.createSession(params.map(_.value).toMap)
  }

  def execute(sessionId: String, params: Param*): ErrorOr[TaskResult] = {

    for {

      session <- repository.getSession(sessionId)

      // can be resumed?

      taskId = session.lastExecution.map(_.taskId).getOrElse(initialTask.id)

      task <- lookup(taskId)

      newVariables = params.map(_.value).toMap

      result <- execute(task, ExecutionContext(sessionId, session.variables ++ newVariables), session)

    } yield result

  }

  private def execute(task: Task, ctx: ExecutionContext, session: Session): ErrorOr[TaskResult] = {

    // save session runnning

    // try catch??
    task.execute(ctx) match {

      case Continue(taskId, next, updatedCtx) =>

        // log?
        println(s"$taskId finished, continue")

        // save session?
        repository.saveSession(session.copy(variables = updatedCtx.variables))

        // execute next
        execute(next, updatedCtx, session)

      case result@Blocked(taskId) =>

        // log?
        println(s"$taskId finished, blocked")

        repository.saveSession(session.blocked(task)).map(_ => result)

      case result@Finished(taskId) =>

        // log?
        println(s"$taskId finished, finished")

        repository.saveSession(session.finished(task)).map(_ => result)

      case result@OnError(taskId, msg) =>

        // log?
        println(s"$taskId finished, on error")

        repository.saveSession(session.onError(task)).map(_ => result)

    }

  }

  /**
    * Cancel an instance of Workflow, it can't be executed again
    *
    * @param sessionId session id
    * @return session id
    */
  def cancel(sessionId:String):ErrorOr[String] = {

    for {

      session <- repository.getSession(sessionId)

      result <- repository.saveSession(session.cancelled())

    } yield result

  }.map(_.id)


  /**
    * It returns a list of every [[Task]] in this workflow
    *
    * @return list of [[Task]]
    */
  def tasks: List[Task] = {
    def tasks(currentTask: Task, accum: List[Task]): List[Task] = {
      if (accum.contains(currentTask)) accum
      else currentTask.followedBy.foldRight(currentTask :: accum)(tasks)
    }

    tasks(initialTask, Nil)
  }

  def lookup(taskId: String): ErrorOr[Task] = tasks.find(_.id == taskId).toRight(TaskNotFound(taskId))

  // draft
  def variables(sessionId: String): Map[String, Any] = repository.getSession(sessionId).map(_.variables).getOrElse(Map.empty)

}