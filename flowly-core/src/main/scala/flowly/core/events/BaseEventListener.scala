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

package flowly.core.events

import java.time.Instant

import flowly.core.repository.model.Session.SessionId
import flowly.core.variables.ReadableExecutionContext

class BaseEventListener extends EventListener {

  def onInitialization(sessionId: SessionId, vars: Map[String, Any]): Unit = ()

  def onStart(sessionId: SessionId, executionContext: ReadableExecutionContext): Unit = ()

  def onResume(sessionId: SessionId, executionContext: ReadableExecutionContext): Unit = ()

  def onContinue(sessionId: SessionId, executionContext: ReadableExecutionContext, currentTask: String, nextTask:String): Unit = ()

  def onSkip(sessionId: SessionId, executionContext: ReadableExecutionContext, currentTask: String, nextTask: String): Unit = ()

  def onBlock(sessionId: SessionId, executionContext: ReadableExecutionContext, currentTask: String): Unit = ()

  def onFinish(sessionId: SessionId, executionContext: ReadableExecutionContext, currentTask: String): Unit = ()

  def onError(sessionId: SessionId, executionContext: ReadableExecutionContext, currentTask: String, cause:Throwable): Unit = ()

  def onCancellation(sessionId: SessionId, reason: String, executionContext: ReadableExecutionContext, currentTask: Option[String]): Unit = ()

  def onRetry(sessionId: SessionId, executionContext: ReadableExecutionContext): Unit = ()

  def onScheduleRetry(sessionId: SessionId, attempts: Int, nextAttempt: Instant, executionContext: ReadableExecutionContext, currentTask: String): Unit = ()
}
