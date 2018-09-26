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

import flowly.repository.model.Session
import flowly.repository.model.Session.{SessionId, Status}
import flowly.tasks.Task
import flowly.variables.ReadableVariables

/**
  * Result of a Workflow execution
  *
  * @param sessionId session id
  * @param taskId    last task executed
  * @param variables current variables
  * @param status    session status
  */
case class Result(sessionId: SessionId, taskId: String, variables: ReadableVariables, status: Status)

object Result {

  def apply(session: Session, task: Task): Result = Result(session.id, task.id, session.variables, session.status)

}
