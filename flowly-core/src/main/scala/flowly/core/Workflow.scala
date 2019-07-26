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
import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.{Block, Continue, Finish, OnError, SkipAndContinue, ToRetry}
import flowly.core.variables.{ExecutionContext, ExecutionContextFactory, Key}


trait Workflow {

  //Validate workflow consistency when constructed
  checkWorkflowConsistency()

  //TODO: It could be useful to validate that current open sessions in DB are blocked in valid tasks for the current Workflow.

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
  def init(params: Param*): ErrorOr[SessionId] = {

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
    session <- repository.getSession(sessionId).filterOrElse(_.isExecutable, SessionCantBeExecuted(sessionId))

    // Get Current Task
    currentTask <- currentTask(session)

    //Are params allowed?
    _ <- currentTask.accept(params.toList)

    executionContext = executionContextFactory.create(session)

    // On Start or Resume Event
    _ = if (currentTask == initialTask && session.lastExecution.isEmpty) eventListeners.foreach(_.onStart(sessionId, executionContext))
    else eventListeners.foreach(_.onResume(sessionId, executionContext))

    // Merge old and new Variables
    currentExecutionContext = executionContext.merge(params.toVariables)

    // Set the session as running
    runningSession <- repository.updateSession(session.resume(currentTask, executionContext.variables))

    // On Start Event
    _ = if(currentTask == initialTask) eventListeners.foreach(_.onStart(sessionId, currentExecutionContext))

    // Execute from Current Task
    result <- execute(currentTask, runningSession, currentExecutionContext)

  } yield result

  private def currentTask(session: Session): ErrorOr[Task] = {
    val taskId = session.lastExecution.map(_.taskId).getOrElse(initialTask.id)

    tasks.find(_.id == taskId).toRight(TaskNotFound(taskId))
  }

  private def execute(task: Task, session: Session, executionContext: ExecutionContext): ErrorOr[ExecutionResult] = {

    def onFailure(cause: Throwable): ErrorOr[ExecutionResult] = Left(ExecutionError(session, task, cause))

    // Execute Task
    task.execute(session.sessionId, executionContext) match {

      case Continue(next, resultingExecutionContext) =>

        repository.updateSession(session.running(next, resultingExecutionContext.variables)).fold(onFailure, { session =>

          // On Continue Event
          eventListeners.foreach(_.onContinue(session.sessionId, resultingExecutionContext, task.id, next.id))

          // Execute next Task
          execute(next, session, resultingExecutionContext)

        })

      case SkipAndContinue(next) =>

        repository.updateSession(session.running(next, executionContext.variables)).fold(onFailure, { session =>

          // On skip and Continue Events
          eventListeners.foreach(l => {
            l.onSkip(session.sessionId, executionContext, task.id, next.id)
            l.onContinue(session.sessionId, executionContext, task.id, next.id)
          })

          // Execute next Task
          execute(next, session, executionContext)

        })

      case Block =>

        repository.updateSession(session.blocked(task)).fold(onFailure, { session =>

          // On Block Event
          eventListeners.foreach(_.onBlock(session.sessionId, executionContext, task.id))

          Right(ExecutionResult(session, executionContext, task))

        })

      case Finish =>

        repository.updateSession(session.finished(task)).fold(onFailure, { session =>

          // On Finish Event
          eventListeners.foreach(_.onFinish(session.sessionId, executionContext, task.id))

          Right(ExecutionResult(session, executionContext, task))

        })

      case ToRetry(cause, nextRetry) =>

        repository.updateSession(session.toRetry(task, cause, nextRetry)).fold(onFailure, { session =>

          // On ToRetry Event
          session.taskAttempts.foreach { taskAttempts =>
            eventListeners.foreach(_.onToRetry(session.sessionId, executionContext, task.id, cause, taskAttempts))
          }

          Left(ExecutionError(session, task, cause))

        })

      case OnError(cause) =>

        repository.updateSession(session.onError(task, cause)).fold(onFailure, { session =>

          // On Error Event
          eventListeners.foreach(_.onError(session.sessionId, executionContext, task.id, cause))

          Left(ExecutionError(session, task, cause))

        })

    }

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

  private def checkWorkflowConsistency(): Unit = {
    val groups = tasks.groupBy(_.id).collect { case (k, v) if v.size > 1 => (k, v.size) }
    if (groups.nonEmpty) throw new IllegalStateException(s"There are repeated Tasks: $groups. Workflow can't be constructed")
  }

}