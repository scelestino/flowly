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

import flowly.core.Param
import flowly.core.variables.{Key, ExecutionContext}

/**
  * [[Task]] is something to do inside a workflow
  *
  * There is no possible to use two identical [[Task]] in the same workflow
  *
  */
trait Task {

  def id: String

  /**
    * Perform a single step inside the workflow. It depends on the task implementation
    */
  private[flowly] def execute(sessionId: String, executionContext: ExecutionContext): TaskResult

  /**
    * Check if all the keys are allowed by this task
    */
  final private[flowly] def accept(params: List[Param]): Boolean = params.forall{case Param(key, value) => allowedKeys.exists( k => k.identifier.contains(key) && k.allowedType(value))}

  /**
    * A list of tasks that follows this task
    */
  private[flowly] def followedBy: List[Task]

  /**
    * A list of keys allowed by this task. It means that a session on this task can be
    * executed with these keys
    */
  protected def allowedKeys: List[Key[_]]

  private[flowly] def taskAllowedKeys = allowedKeys

  override def toString: String = s"Task:$id"

}