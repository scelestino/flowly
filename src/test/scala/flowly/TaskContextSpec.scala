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

package flowly

import flowly.tasks.context.{Key, TaskContext}
import org.scalatest.{FlatSpec, Matchers}

class TaskContextSpec extends FlatSpec with Matchers {

  case object Key1 extends Key[String]

//  "ExecutionContext" should "be immutable" in {
//
//    val ctx = new TaskContext("1", Map.empty)
//
//    ctx.set(Key1, "value1")
//
//    ctx.contains(Key1) shouldBe false
//
//  }

//  it should "set new values" in {
//
//    val ctx = new TaskContext("1", Map.empty)
//
//    val ctx2 = ctx.set(Key1, "value1")
//
//    ctx2.contains(Key1) shouldBe true
//
//  }

//  it should "unset values" in {
//
//    val ctx = new TaskContext("1", Map("key1" -> "value1"))
//
//    val ctx2 = ctx.unset(Key1)
//
//    ctx2.contains(Key1) shouldBe false
//
//  }

}
