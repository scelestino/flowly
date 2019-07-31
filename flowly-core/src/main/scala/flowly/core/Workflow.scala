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
  checkConsistency()

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

    repository.insertSession(Session(params.toList.toVariables)).map { session =>

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
  def execute(sessionId: SessionId, params: Param*): ErrorOr[ExecutionResult] = execute(sessionId, params.toList)

  /**
    * Execute an instance of [[Workflow]] form its current [[Task]] with the given params
    *
    * @param sessionId session id
    * @param params    new workflow variables
    * @return execution result
    */
  def execute(sessionId: SessionId, params: List[Param]): ErrorOr[ExecutionResult] = for {

    // Get Session, can be executed?
    session <- repository.getSession(sessionId).filterOrElse(_.isExecutable, SessionCantBeExecuted(sessionId))

    // Get Current Task
    currentTask <- currentTask(session)

    //Are params allowed?
    _ <- if(currentTask.accept(params.toKeys)) Right(true) else Left(ParamsNotAllowed(params))

    // Set the session as running
    runningSession <- repository.updateSession(session.resume(currentTask, params))

    // Create Execution Context
    executionContext = executionContextFactory.create(session)

    // On Start or Resume Event
    _ = session.lastExecution.fold(eventListeners.foreach(_.onStart(sessionId, executionContext)) ) { _ =>
      eventListeners.foreach(_.onResume(sessionId, executionContext))
    }

    // Execute from Current Task
    result <- execute(currentTask, runningSession, executionContext)

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

        repository.updateSession(session.continue(next, resultingExecutionContext)).fold(onFailure, { session =>

          // On Continue Event
          eventListeners.foreach(_.onContinue(session.sessionId, resultingExecutionContext, task.id, next.id))

          // Execute next Task
          execute(next, session, resultingExecutionContext)

        })

      case SkipAndContinue(next) =>

        repository.updateSession(session.continue(next, executionContext)).fold(onFailure, { session =>

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

  /**
    * Check if there are duplicated tasks
    */
  private def checkConsistency(): Unit = {
    val duplicated = tasks.map(_.id).diff(tasks.map(_.id).distinct)
    if (duplicated.nonEmpty) throw new IllegalStateException(s"There are repeated Tasks: $duplicated. Workflow can't be constructed")
  }

}