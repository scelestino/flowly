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

import flowly.core.repository.model.Session
import flowly.core.repository.model.Session.{SessionId, Status}
import flowly.core.tasks.basic.Task
import flowly.core.variables.ReadableExecutionContext

/**
  * Result of a Workflow execution
  *
  * @param sessionId session id
  * @param taskId    last task executed
  * @param executionContext current execution context
  * @param status    session status
  */
case class ExecutionResult private[flowly](sessionId: SessionId, taskId: String, executionContext: ReadableExecutionContext, status: Status)

object ExecutionResult {

  def apply(session: Session, executionContext: ReadableExecutionContext, task: Task): ExecutionResult = ExecutionResult(session.sessionId, task.id, executionContext, session.status)

}
