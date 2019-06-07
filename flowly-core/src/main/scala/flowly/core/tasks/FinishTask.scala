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

import flowly.core.variables.{Key, Variables}

/**
  * An instance of this [[Task]] is need be to used to finish a workflow execution.
  *
  * It is possible to configure multiple [[FinishTask]] inside the same workflow.
  *
  * Once an execution reach this kind of [[Task]], the workflow instance where it is used will finish.
  *
  * @param id task id
  */
case class FinishTask(id: String) extends Task {

  final private[flowly] def execute(sessionId: String, variables: Variables): TaskResult = Finish

  final private[flowly] def followedBy: List[Task] = Nil

  override protected def allowedKeys: List[Key[_]] = List.empty

}