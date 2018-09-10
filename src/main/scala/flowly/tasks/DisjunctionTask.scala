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
import flowly.tasks.context.{ReadableTaskContext, TaskContext}
import flowly.tasks.result.{Continue, OnError, TaskResult}

/**
  * An instance of this [[Task]] will choose a branch of execution between different paths based on given conditions.
  *
  * It will test each condition until find any that works. If no condition works, this [[Task]] will fail.
  *
  */
trait DisjunctionTask extends Task {

  def branches: List[(ReadableTaskContext => Boolean, Task)]

  def execute(ctx: TaskContext): TaskResult = try {
    next(ctx) match {
      case Some(next) => Continue(id, next, ctx)
      case None => OnError(id, DisjunctionTaskError())
    }
  } catch {
    case throwable: Throwable => OnError(id, throwable)
  }

  def followedBy: List[Task] = branches.collect { case (_, task) => task }

  private def next(ctx: TaskContext): Option[Task] = branches.collectFirst { case (condition, task) if condition(ctx) => task }

}

object DisjunctionTask {

  def apply(_id: String, _branches: (ReadableTaskContext => Boolean, Task)*): DisjunctionTask = new DisjunctionTask {
    def id: String = _id

    def branches: List[(ReadableTaskContext => Boolean, Task)] = _branches.toList
  }

  def apply(_id: String, ifTrue: Task, ifFalse: Task, condition: ReadableTaskContext => Boolean): DisjunctionTask = {
    apply(_id, (condition, ifTrue), (_ => true, ifFalse))
  }

}