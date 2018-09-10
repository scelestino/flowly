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

package flowly.tasks

import flowly.tasks.context.{ReadableTaskContext, TaskContext}
import flowly.tasks.result.{Blocked, Continue, OnError, TaskResult}

/**
  * An instance of this [[Task]] could block the execution if a given condition fails.
  *
  * Conditions can be setted throught the execution context.
  *
  */
trait BlockingTask extends SingleTask {

  def condition(ctx: ReadableTaskContext): Boolean

  def execute(ctx: TaskContext): TaskResult = try {
    if (condition(ctx)) Continue(id, next, ctx) else Blocked(id)
  } catch {
    case throwable: Throwable => OnError(id, throwable)
  }

}

object BlockingTask {

  def apply(_id: String, _next: Task, _condition: ReadableTaskContext => Boolean): BlockingTask = new BlockingTask {
    def id: String = _id

    def next: Task = _next

    def condition(ctx: ReadableTaskContext): Boolean = _condition(ctx)
  }

}