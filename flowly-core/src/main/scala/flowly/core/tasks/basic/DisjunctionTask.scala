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

import flowly.core.DisjunctionTaskError
import flowly.core.tasks.model.{Block, Continue, OnError, TaskResult}
import flowly.core.context.{ExecutionContext, Key, ReadableExecutionContext}

/**
  * An instance of this [[Task]] will choose a branch of execution between different paths based on given conditions.
  *
  * It will test each condition until find any that works. If no condition works, this [[Task]] will fail or block depending on blockOnNoCondition value.
  *
  */
trait DisjunctionTask extends Task {

  protected def branches: List[(ReadableExecutionContext => Boolean, Task)]

  /**
    * This task is going to block instead of fail when there are no conditions that match
    */
  protected def blockOnNoCondition:Boolean

  private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult = try {
    branches.collectFirst { case (condition, task) if condition(executionContext) => task } match {
      case Some(next) => Continue(next, executionContext)
      case None if blockOnNoCondition => Block
      case None => OnError(DisjunctionTaskError(id))
    }
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

  private[flowly] def followedBy: List[Task] = branches.collect { case (_, task) => task }

}

