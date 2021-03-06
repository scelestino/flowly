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

package flowly.core.tasks.strategies.stopping

import flowly.core.context.ReadableExecutionContext
import flowly.core.repository.model.Attempts

class QuantityStoppingStrategy(maxAttempts: Int) extends StoppingStrategy {

  def shouldRetry(executionContext: ReadableExecutionContext, attempts: Attempts): Boolean = {
    attempts.quantity < maxAttempts
  }

}