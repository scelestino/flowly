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

package flowly.core.tasks.strategies.scheduling

import java.time.Instant

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts

import scala.annotation.tailrec

class FibonacciSchedulingStrategy(multiplier: Int, upperBound: Int) extends SchedulingStrategy {

  def nextRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Instant = {
    Instant.now.plusSeconds( upperBound.min( multiplier * fibonacci(attempts.quantity) ))
  }

  private def fibonacci(i: Int): Int = {
    @tailrec
    def fibTail(i: Int, a: Int, b: Int): Int = i match {
      case 0 => a
      case _ => fibTail( i - 1, b, a + b )
    }
    fibTail(i, 0, 1)
  }

}
