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

import flowly.DisjunctionTaskError
import flowly.variables.{ReadableVariables, Variables}

/**
  * An instance of this [[Task]] will choose a branch of execution between different paths based on given conditions.
  *
  * It will test each condition until find any that works. If no condition works, this [[Task]] will fail.
  *
  */
trait DisjunctionTask extends Task {

  def branches: List[(ReadableVariables => Boolean, Task)]

  def execute(sessionId: String, variables: Variables): TaskResult = try {
    next(variables) match {
      case Some(next) => Continue(next, variables)
      case None => OnError(DisjunctionTaskError())
    }
  } catch {
    case throwable: Throwable => OnError(throwable)
  }

  def followedBy: List[Task] = branches.collect { case (_, task) => task }

  private def next(variables: ReadableVariables): Option[Task] = branches.collectFirst { case (condition, task) if condition(variables) => task }

}

object DisjunctionTask {

  def apply(_id: String, _branches: (ReadableVariables => Boolean, Task)*): DisjunctionTask = new DisjunctionTask {
    def id: String = _id

    def branches: List[(ReadableVariables => Boolean, Task)] = _branches.toList
  }

  def apply(_id: String, ifTrue: Task, ifFalse: Task, condition: ReadableVariables => Boolean): DisjunctionTask = {
    apply(_id, (condition, ifTrue), (_ => true, ifFalse))
  }

}