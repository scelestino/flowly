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

package flowly.core.tasks

import flowly.core.ErrorOr
import flowly.core.variables.Variables

/**
  * An instance of this [[Task]] will execute your code and can change the execution context.
  *
  */
trait ExecutionTask extends SingleTask {

  def execute(sessionId: String, variables: Variables): TaskResult = try {
    perform(sessionId, variables).fold(OnError, Continue(next, _))
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

  protected def perform(sessionId: String, variables: Variables): ErrorOr[Variables]

}

object ExecutionTask {

  def apply(_id: String, _next: Task)(_perform: (String, Variables) => ErrorOr[Variables]): ExecutionTask = new ExecutionTask {

    def id: String = _id

    def next: Task = _next

    def perform(sessionId: String, variables: Variables): ErrorOr[Variables] = _perform(sessionId, variables)

  }

}

// TODO: an execution task could cancel a workflow?????