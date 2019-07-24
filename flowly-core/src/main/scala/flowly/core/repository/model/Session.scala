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

import java.time.LocalDateTime
import java.util.UUID

import flowly.core.repository.model.Session.{SessionId, Status}
import flowly.core.repository.model.Status._
import flowly.core.tasks.basic.Task
import flowly.core.variables.ExecutionContext

case class Session(sessionId: SessionId, variables: Map[String, Any], lastExecution: Option[Execution], cancellation: Option[Cancellation], createdAt: LocalDateTime, status: Status, version: Long) {

  def running(task: Task, variables: Map[String, Any]): Session = changeStatus(task, variables, RUNNING)

  def blocked(task: Task): Session = changeStatus(task, variables, BLOCKED)

  def finished(task: Task): Session = changeStatus(task, variables, FINISHED)

  def onError(task: Task, throwable: Throwable): Session = changeStatus(task, variables, ERROR)

  def toRetry(task: Task, throwable: Throwable): Session = changeStatus(task, variables, TO_RETRY)

  def cancelled(reason: String): Session = copy(cancellation = Option(Cancellation(reason)), status = CANCELLED)

  def isExecutable: Boolean = status match {
    case RUNNING | FINISHED | CANCELLED => false
    case _ => true
  }

  private def changeStatus(task: Task, variables: Map[String, Any], status: Status): Session = {
    copy(lastExecution = Option(Execution(task.id)), variables = variables, status = status)
  }

}

object Session {

  type SessionId = String
  type Status    = String

  def apply(id: SessionId, variables: Map[String, Any]): Session = {
    new Session(id, variables, None, None, LocalDateTime.now, CREATED, 0L)
  }

  def apply(variables: Map[String, Any]): Session = apply(UUID.randomUUID.toString, variables)
}

case class Execution(taskId: String, at: LocalDateTime)

object Execution {
  def apply(taskId: String): Execution = new Execution(taskId, LocalDateTime.now)
}

case class Cancellation(reason: String, at: LocalDateTime)

object Cancellation {
  def apply(reason: String): Cancellation = new Cancellation(reason, LocalDateTime.now)
}