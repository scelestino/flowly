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

import flowly.variables.Variables

/**
  * Interface of a [[Task]] execution result
  */
trait TaskResult

/**
  * Current workflow execution must be continued
  *
  * @param currentTask task executed
  * @param nextTask  next task to be executed
  * @param variables current variables
  */
case class Continue(currentTask: Task, nextTask: Task, variables: Variables) extends TaskResult

/**
  * Current workflow execution has finished
  *
  * @param currentTask task id
  */
case class Finish(currentTask: Task) extends TaskResult

/**
  * Current workflow execution cannot continue because a condition is not met
  *
  * @param currentTask task id
  */
case class Block(currentTask: Task) extends TaskResult

/**
  * There was an unexpected error during current workflow execution
  *
  * @param currentTask task id
  * @param cause  error
  */
case class OnError(currentTask: Task, cause: Throwable) extends TaskResult