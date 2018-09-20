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

import flowly.{DisjunctionTaskError, IntKey, StringKey}
import org.specs2.mutable.Specification

class DisjunctionTaskSpec extends Specification {

  "DisjunctionTask" should {

    "continue if execution was successful" in new Context {
      val task = DisjunctionTask("1", FinishTask("2"), FinishTask("3"), _.contains(StringKey))
      task.execute("session1", variables) must haveClass[Continue]
    }

    "after a continue next task must be correct" in new Context {
      val ifTrue = FinishTask("2")
      val task = DisjunctionTask("1", ifTrue, FinishTask("3"), _.contains(StringKey))
      task.execute("session1", variables) match {
        case Continue(nextTask, _) => nextTask must_=== ifTrue
        case otherwise => failure(s"$otherwise must be Continue")
      }
    }

    "error if there no valid condition" in new Context {
      val task = DisjunctionTask("1", (_.contains(IntKey), FinishTask("2")))
      task.execute("session1", variables) must_== OnError(DisjunctionTaskError())
    }

  }

}