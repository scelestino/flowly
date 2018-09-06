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

import flowly.context.ExecutionContext

/**
  * An instance of this [[Task]] will execute your code and can change the execution context.
  *
  */
trait ExecutionTask extends SingleTask {

  def execute(ctx: ExecutionContext): TaskResult = try {
    perform(ctx).fold(Error(id, _), Continue(id, next, _))
  } catch {
    case e: Throwable => Error(id, e.getMessage)
  }

  protected def perform(ctx: ExecutionContext): ExecutionTaskResult

}

object ExecutionTask {

  def apply(_id: String, _next: Task)(_perform: ExecutionContext => ExecutionTaskResult): ExecutionTask = new ExecutionTask {
    def id: String = _id

    def next: Task = _next

    def perform(ctx: ExecutionContext): ExecutionTaskResult = _perform(ctx)
  }

}

