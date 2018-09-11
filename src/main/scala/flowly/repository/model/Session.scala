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

package flowly.repository.model

import java.time.LocalDateTime

import flowly.Param
import flowly.repository.model.WFStatus.WFStatus
import flowly.tasks.Task
import flowly.tasks.context.TaskContext

case class Session private(id: String, lastExecution: Option[Execution], variables: Map[String, Any], cancellation: Option[Cancellation], createdAt: LocalDateTime, status: String) {

  def running(task: Task, params:Param*): Session = changeStatus(task, variables ++ params.toVariables, WFStatus.RUNNING)

  def running(task: Task, taskContext:TaskContext): Session = changeStatus(task, taskContext.variables, WFStatus.RUNNING)

  def blocked(task: Task): Session = changeStatus(task, variables, WFStatus.BLOCKED)

  def finished(task: Task): Session = changeStatus(task, variables, WFStatus.FINISHED)

  def onError(task: Task): Session = changeStatus(task, variables, WFStatus.ERROR)

  def cancelled(reason:String): Session = copy(cancellation = Option(Cancellation(reason)), status = WFStatus.CANCELLED)

  def isExecutable: Boolean = WFStatus.withName(status) match {
    case WFStatus.RUNNING | WFStatus.FINISHED | WFStatus.CANCELLED => false
    case _ => true
  }

  private def changeStatus(task: Task, variables:Map[String,Any], status: WFStatus): Session = {
    copy(lastExecution = Option(Execution(task.id)), variables = variables, status = status)
  }

}

object Session {
  def apply(id: String, variables: Map[String, Any]): Session = {
    new Session(id, None, variables, None, LocalDateTime.now, WFStatus.CREATED)
  }
}

case class Execution(taskId: String, at: LocalDateTime)

object Execution {
  def apply(taskId: String): Execution = new Execution(taskId, LocalDateTime.now)
}

case class Cancellation(reason:String, at:LocalDateTime)

object Cancellation {
  def apply(reason: String): Cancellation = new Cancellation(reason, LocalDateTime.now)
}