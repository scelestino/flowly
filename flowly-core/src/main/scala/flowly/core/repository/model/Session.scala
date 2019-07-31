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

package flowly.core.repository.model

import java.time.{Instant, LocalDateTime}
import java.util.UUID

import flowly.core.repository.model.Session.{SessionId, Status}
import flowly.core.repository.model.Status._
import flowly.core.tasks.basic.Task
import flowly.core.tasks.model.TaskAttempts

case class Session(sessionId: SessionId, variables: Map[String, Any], lastExecution: Option[Execution], taskAttempts:Option[TaskAttempts], cancellation: Option[Cancellation], createdAt: LocalDateTime, status: Status, version: Long) {

  def resume(task: Task, variables: Map[String, Any]): Session = {
    copy(lastExecution = Option(Execution(task.id)), variables = variables, status = RUNNING, taskAttempts = taskAttempts.map(_.newAttempt()))
  }

  def continue(task: Task, variables: Map[String, Any]): Session = {
    copy(lastExecution = Option(Execution(task.id)), variables = variables, status = RUNNING, taskAttempts = None)
  }

  def blocked(task: Task): Session = {
    copy(lastExecution = Option(Execution(task.id)), status = BLOCKED, taskAttempts = None)
  }

  def finished(task: Task): Session = {
    copy(lastExecution = Option(Execution(task.id)), status = FINISHED, taskAttempts = None)
  }

  def onError(task: Task, throwable: Throwable): Session = {
    copy(lastExecution = Option(Execution(task.id)), status = ERROR, taskAttempts = Option(taskAttempts.getOrElse(TaskAttempts()).stopRetrying()))
  }

  def toRetry(task: Task, throwable: Throwable, nextRetry: Instant): Session = {
    copy(lastExecution = Option(Execution(task.id, throwable.getMessage)), status = TO_RETRY, taskAttempts = Option(taskAttempts.getOrElse(TaskAttempts()).withNextRetry(nextRetry)))
  }

  def cancelled(reason: String): Session = {
    copy(cancellation = Option(Cancellation(reason)), status = CANCELLED, taskAttempts = None)
  }

  def isExecutable: Boolean = status match {
    case RUNNING | FINISHED | CANCELLED => false
    case _ => true
  }

}

object Session {

  type SessionId = String
  type Status    = String

  def apply(id: SessionId, variables: Map[String, Any]): Session = {
    new Session(id, variables, None, None, None, LocalDateTime.now, CREATED, 0L)
  }

  def apply(variables: Map[String, Any]): Session = apply(UUID.randomUUID.toString, variables)
}

case class Execution(taskId: String, message:Option[String], at: Instant)

object Execution {
  def apply(taskId: String): Execution = new Execution(taskId, None, Instant.now)
  def apply(taskId: String, message:String): Execution = new Execution(taskId, Option(message), Instant.now)
}

case class Cancellation(reason: String, at: Instant)

object Cancellation {
  def apply(reason: String): Cancellation = new Cancellation(reason, Instant.now)
}