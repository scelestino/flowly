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

import flowly.repository.Repository
import flowly.repository.model.Session
import flowly.tasks._
import flowly.variables.{ReadableVariables, Variables}

trait Workflow {

  def initialTask: Task

  // temp
  val repository = new Repository

  /**
    * Initialize a new workflow session
    *
    * @param params initial workflow variables
    * @return session id
    */
  def init(params: Param*): ErrorOr[String] = {
    repository.createSession(params.toVariables)
  }

  /**
    * Execute an instance of [[Workflow]] form its current [[Task]] with the given params
    *
    * @param sessionId session id
    * @param params    new workflow variables
    * @return execution result
    */
  def execute(sessionId: String, params: Param*): ErrorOr[Result] = {

    for {

      // Get a Session, can be executed?
      currentSession <- repository.getSession(sessionId).filterOrElse(_.isExecutable, SessionCantBeExecuted(sessionId))

      // Get current Task
      currentTask <- {

        val taskId = currentSession.lastExecution.map(_.taskId).getOrElse(initialTask.id)

        tasks.find(_.id == taskId).toRight(TaskNotFound(taskId))

      }

      // Merge old and new Variables
      currentVariables = currentSession.variables.merge(params.toVariables)

      // Execute current Task
      result <- execute(currentTask, currentSession, currentVariables)

    } yield result

  }

  private def execute(task: Task, session: Session, variables:Variables): ErrorOr[Result] = {

    // Update Session to Running Status
    repository.saveSession(session.running(task, variables)).flatMap { currentSession =>

      // Execute Task
      task.execute(currentSession.id, variables) match {

        case Continue(taskId, next, currentVariables) =>

          // log?
          println(s"$taskId continue")

          // Execute next Task
          execute(next, currentSession, currentVariables)

        case Block(taskId) =>

          // log?
          println(s"$taskId blocked")

          // how to get result
          repository.saveSession(currentSession.blocked(task)).map(s => {
            Result(s.id, taskId, s.variables, s.status)
          })

        case Finish(taskId) =>

          // log?
          println(s"$taskId finished")

          // how to get result
          repository.saveSession(currentSession.finished(task)).map(s => {
            Result(s.id, taskId, s.variables, s.status)
          })

        case OnError(taskId, msg) =>

          // log?
          println(s"$taskId on error")

          // how to get result
          repository.saveSession(currentSession.onError(task)).map(s => {
            Result(s.id, taskId, s.variables, s.status)
          })
        // TODO: tasks result can be Cancelled???

      }

    }

  }

  /**
    * Cancel an instance of Workflow, it can't be executed again
    *
    * @param sessionId session id
    * @return session id
    */
  def cancel(sessionId: String, reason:String): ErrorOr[String] = {

    for {

      session <- repository.getSession(sessionId)

      result <- repository.saveSession(session.cancelled(reason))

    } yield result

  }.map(_.id)


  /**
    * It returns a list of every [[Task]] in this workflow
    *
    * @return list of [[Task]]
    */
  private def tasks: List[Task] = {
    def tasks(currentTask: Task, accum: List[Task]): List[Task] = {
      if (accum.contains(currentTask)) accum
      else currentTask.followedBy.foldRight(currentTask :: accum)(tasks)
    }

    tasks(initialTask, Nil)
  }

  /**
    * It returns Variables for a given session id
    *
    * @param sessionId session id
    * @return Variables (read only)
    */
  def variables(sessionId: String): ErrorOr[ReadableVariables] = repository.getSession(sessionId).map(_.variables)

}