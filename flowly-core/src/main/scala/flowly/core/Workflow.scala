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

package flowly.core

import flowly.core.events.EventListener
import flowly.core.repository.Repository
import flowly.core.repository.model.Session
import flowly.core.repository.model.Session.SessionId
import flowly.core.tasks._
import flowly.core.variables.{ExecutionContext, ExecutionContextFactory}


trait Workflow {

  def initialTask: Task

  def eventListeners: List[EventListener] = Nil

  val executionContextFactory: ExecutionContextFactory

  val repository: Repository

  /**
    * Initialize a new workflow session
    *
    * @param params initial workflow variables
    * @return session id
    */
  def init(params: Param*): ErrorOr[String] = {

    repository.insertSession(Session(params.toVariables)).map { session =>

      // On Initialization Event
      eventListeners.foreach(_.onInitialization(session.sessionId, session.variables))

      session.sessionId

    }

  }

  /**
    * Execute an instance of [[Workflow]] form its current [[Task]] with the given params
    *
    * @param sessionId session id
    * @param params    new workflow variables
    * @return execution result
    */
  def execute(sessionId: SessionId, params: Param*): ErrorOr[ExecutionResult] = for {

    // Get Session, can be executed?
    currentSession <- repository.getSession(sessionId).filterOrElse(_.isExecutable, SessionCantBeExecuted(sessionId))

    // Get Current Task
    currentTask <- currentTask(currentSession)

    //Are params allowed?
    _ <- currentTask.accept(params.toList)

    executionContext = executionContextFactory.create(currentSession)

    // On Start Event
    _ = if (currentTask == initialTask) eventListeners.foreach(_.onStart(sessionId, executionContext))

    // Merge old and new Variables
    currentExecutionContext = executionContext.merge(params.toVariables)

    // Execute from Current Task
    result <- execute(currentTask, currentSession, currentExecutionContext)

  } yield result

  private def currentTask(session: Session): ErrorOr[Task] = {
    val taskId = session.lastExecution.map(_.taskId).getOrElse(initialTask.id)

    tasks.find(_.id == taskId).toRight(TaskNotFound(taskId))
  }

  private def execute(task: Task, session: Session, executionContext: ExecutionContext): ErrorOr[ExecutionResult] = {

    def onSuccess(currentSession: Session): ErrorOr[ExecutionResult] = {

      // Execute Task
      task.execute(currentSession.sessionId, executionContext) match {

        case Continue(next, currentExecutionContext) =>

          // On Continue Event
          eventListeners.foreach(_.onContinue(session.sessionId, currentExecutionContext, task.id, next.id))

          // Execute next Task
          execute(next, currentSession, currentExecutionContext)

        case Block =>

          repository.updateSession(currentSession.blocked(task)).fold(onFailure, { session =>

            // On Block Event
            eventListeners.foreach(_.onBlock(session.sessionId, executionContext, task.id))

            Right(ExecutionResult(session, executionContext, task))

          })

        case Finish =>

          repository.updateSession(currentSession.finished(task)).fold(onFailure, { session =>

            // On Finish Event
            eventListeners.foreach(_.onFinish(session.sessionId, executionContext, task.id))

            Right(ExecutionResult(session, executionContext, task))

          })

        case OnError(cause) =>

          // TODO: if repo fails I lost original cause
          repository.updateSession(currentSession.onError(task, cause)).fold(onFailure, { session =>

            // On Error Event
            eventListeners.foreach(_.onError(session.sessionId, executionContext, task.id, cause))

            Left(ExecutionError(session, task, cause))

          })

      }

    }

    def onFailure(cause: Throwable): ErrorOr[ExecutionResult] = Left(ExecutionError(session, task, cause))

    // Update Session to Running Status and execute it
    repository.updateSession(session.running(task, executionContext.variables)).fold(onFailure, onSuccess)

  }

  /**
    * Cancel an instance of Workflow, it can't be executed again
    *
    * @param sessionId session id
    * @return session id
    */
  def cancel(sessionId: SessionId, reason: String): ErrorOr[SessionId] = {

    for {

      session <- repository.getSession(sessionId)

      result <- repository.updateSession(session.cancelled(reason))

      // On Cancellation Event
      _ = eventListeners.foreach(_.onCancellation(sessionId, reason, executionContextFactory.create(session), session.lastExecution.map(_.taskId)))

    } yield result

  }.map(_.sessionId)

  /**
    * It returns Variables for a given session id
    *
    * @param sessionId session id
    * @return Variables (read only)
    */
  def variables(sessionId: SessionId): ErrorOr[Map[String, Any]] = repository.getSession(sessionId).map(_.variables)

  def currentAllowedKeys(sessionId: String): ErrorOr[List[Key[_]]] = for {

    session <- repository.getSession(sessionId)

    currentTask <- currentTask(session)

  } yield currentTask.allowedKeys


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

}