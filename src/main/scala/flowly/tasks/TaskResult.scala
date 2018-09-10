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
  * Interface of a [[Task]] execution result
  */
trait TaskResult {
  def taskId:String
}

/**
  * Current workflow execution must be continued
  *
  * @param taskId task id
  * @param nextTask next task to be executed
  * @param ctx current execution context
  */
case class Continue(taskId:String, nextTask:Task, ctx: ExecutionContext) extends TaskResult

/**
  * Current workflow execution has finished
  *
  * @param taskId task id
  */
case class Finished(taskId:String) extends TaskResult

/**
  * Current workflow execution cannot continue because a condition is not met
  *
  * @param taskId task id
  */
case class Blocked(taskId:String) extends TaskResult

/**
  * There was an unexpected error during current workflow execution
  *
  * @param taskId task id
  * @param msg error message
  */
case class OnError(taskId:String, msg:String) extends TaskResult