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

import flowly.core.variables.{Key, ReadableExecutionContext, ExecutionContext}


/**
  * An instance of this [[Task]] could block the execution if a given condition fails.
  *
  * Conditions can be setted throught the execution context.
  *
  */
trait BlockingTask extends SingleTask {

  protected def condition(variables: ReadableExecutionContext): Boolean

  final private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = try {
    if (condition(executionContext)) Continue(next, executionContext) else Block
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

}

object BlockingTask {

  def apply(_id: String, _next: Task, _condition: ReadableExecutionContext => Boolean, _allowedKeys: List[Key[_]]): BlockingTask = new BlockingTask {

    override val id: String = _id

    val next: Task = _next

    def condition(variables: ReadableExecutionContext): Boolean = _condition(variables)

    override def allowedKeys: List[Key[_]] = _allowedKeys
  }

}