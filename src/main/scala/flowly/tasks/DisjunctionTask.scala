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

import flowly.context.{ExecutionContext, ReadableExecutionContext}

/**
  * An instance of this [[Task]] will choose a branch of execution between different paths based on given conditions.
  *
  * It will test each condition until find any that works. If no condition works, this [[Task]] will fail.
  *
  */
trait DisjunctionTask extends Task {

  def branches: List[(ReadableExecutionContext => Boolean, Task)]

  def execute(ctx: ExecutionContext): TaskResult = {
    next(ctx) match {
      case Some(next) => Continue(id, next, ctx)
      case None => OnError(id, "DisjunctionTask is not exhaustive!")
    }
  }

  def followedBy: List[Task] = branches.collect { case (_, task) => task }

  private def next(ctx: ExecutionContext): Option[Task] = branches.collectFirst { case (condition, task) if condition(ctx) => task }

}

object DisjunctionTask {

  def apply(_id: String, _branches: (ReadableExecutionContext => Boolean, Task)*): DisjunctionTask = new DisjunctionTask {
    def id: String = _id

    def branches: List[(ReadableExecutionContext => Boolean, Task)] = _branches.toList
  }

  def apply(_id: String, ifTrue: Task, ifFalse: Task, condition: ReadableExecutionContext => Boolean): DisjunctionTask = {
    apply(_id, (condition, ifTrue), (_ => true, ifFalse))
  }

}