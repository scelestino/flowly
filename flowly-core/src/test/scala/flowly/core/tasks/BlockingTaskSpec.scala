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

import flowly.core.tasks.basic.FinishTask
import flowly.core.tasks.model.{Block, Continue, OnError}
import flowly.core.{BooleanKey, StringKey, TasksContext, tasks}
import org.specs2.mutable.Specification

class BlockingTaskSpec extends Specification {

  "BlockingTask" should {

    "block execution if condition is false" in new TasksContext {
      val task = tasks.BlockingTask("1", FinishTask("2"), _.contains(BooleanKey), List(StringKey))
      task.execute("session1", ec) must_== Block
    }

    "continue execution if condition is true" in new TasksContext {
      val task = tasks.BlockingTask("1", FinishTask("2"), _.contains(StringKey), List(StringKey))
      task.execute("session1", ec) must haveClass[Continue]
    }

    "after a continue variables must be the same" in new TasksContext {
      val task = tasks.BlockingTask("1", FinishTask("2"), _.contains(StringKey), List(StringKey))
      task.execute("session1", ec) match {
        case Continue(_, v) => v must_=== ec
        case otherwise => failure(s"$otherwise must be Continue")
      }
    }

    "after a continue next task must be correct" in new TasksContext {
      val task = tasks.BlockingTask("1", FinishTask("2"), _.contains(StringKey), List(StringKey))
      task.execute("session1", ec) match {
        case Continue(nextTask, _) => nextTask must_=== task.next
        case otherwise => failure(s"$otherwise must be Continue")
      }
    }

    "error if execution was unsuccessful" in new TasksContext {
      val task = tasks.BlockingTask("1", FinishTask("2"), _ => throw TestException("execution error"), List(StringKey))
      task.execute("session1", ec) match {
        case OnError(TestException(message)) => message must_== "execution error"
        case otherwise => failure(s"$otherwise must be OnError")
      }
    }

  }

}

