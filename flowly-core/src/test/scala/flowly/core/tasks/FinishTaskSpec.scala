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

import flowly.core.TasksContext
import flowly.core.tasks.basic.FinishTask
import flowly.core.tasks.model.Finish
import org.specs2.mutable.Specification

class FinishTaskSpec extends Specification{

  "FinishTask" should {

    "finish execution" in new TasksContext {
      FinishTask("1").execute("session1", ec) must_=== Finish
    }

  }

}
