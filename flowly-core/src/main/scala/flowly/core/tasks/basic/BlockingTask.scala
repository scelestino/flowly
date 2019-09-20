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

package flowly.core.tasks.basic

import flowly.core.tasks.model.{Block, Continue, OnError, TaskResult}
import flowly.core.context.{ExecutionContext, ReadableExecutionContext}


/**
  * An instance of this [[Task]] could block the execution if a given condition fails.
  *
  * Conditions can be setted throught the execution context.
  *
  */
trait BlockingTask extends Task {

  val next: Task

  protected def condition(executionContext: ReadableExecutionContext): Boolean

  private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = try {
    if (condition(executionContext)) Continue(next, executionContext) else Block
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

  private[flowly] def followedBy: List[Task] = next :: Nil

}

