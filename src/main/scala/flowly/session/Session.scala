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

package flowly.session

import java.time.LocalDateTime

import flowly.WFStatus
import flowly.WFStatus.WFStatus
import flowly.tasks.Task

case class Session(id: String, lastExecution: Option[Execution], variables: Map[String, Any], createdAt: LocalDateTime, status:String) {

  def running(task:Task):Session = changeStatus(task, WFStatus.RUNNING)

  def blocked(task:Task):Session = changeStatus(task, WFStatus.BLOCKED)

  def finished(task:Task):Session = changeStatus(task, WFStatus.FINISHED)

  def onError(task:Task):Session = changeStatus(task, WFStatus.ERROR)

  def cancelled():Session = copy(status = WFStatus.CANCELLED)

  private def changeStatus(task:Task, status:WFStatus):Session = {
    copy(lastExecution = Option(Execution(task.id, LocalDateTime.now)), status = status)
  }

}

object Session {

  def apply(id: String, variables: Map[String, Any]): Session = {
    new Session(id, None, variables, LocalDateTime.now, WFStatus.CREATED)
  }

}

case class Execution(taskId: String, at: LocalDateTime)