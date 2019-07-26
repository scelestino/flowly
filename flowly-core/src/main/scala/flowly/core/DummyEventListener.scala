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

package flowly.core

import java.time.Instant

import flowly.core.events.EventListener
import flowly.core.repository.model.Session.SessionId
import flowly.core.tasks.model.TaskAttempts
import flowly.core.variables.ReadableExecutionContext

class DummyEventListener extends EventListener {

  def onInitialization(sessionId: SessionId, vars: Map[String, Any]): Unit = {
    println(s"Init a new instance $sessionId")
  }

  def onStart(sessionId: SessionId, variables: ReadableExecutionContext): Unit = {
    println(s"session $sessionId just started")
  }

  def onResume(sessionId: SessionId, executionContext: ReadableExecutionContext): Unit = {
    println(s"session $sessionId has been resumed")
  }

  def onContinue(sessionId: SessionId, variables: ReadableExecutionContext, currentTask: String, nextTask:String): Unit = {
    println(s"session $sessionId task $currentTask executed, next $nextTask")
  }

  def onSkip(sessionId: SessionId, variables: ReadableExecutionContext, currentTask: String, nextTask: String): Unit = {
    println(s"session $sessionId task $currentTask skipped, next $nextTask")
  }

  def onBlock(sessionId: SessionId, variables: ReadableExecutionContext, currentTask: String): Unit = {
    println(s"session $sessionId task $currentTask blocked")
  }

  def onFinish(sessionId: SessionId, variables: ReadableExecutionContext, currentTask: String): Unit = {
    println(s"session $sessionId just finished")
  }

  def onError(sessionId: SessionId, variables: ReadableExecutionContext, currentTask: String, cause:Throwable): Unit = {
    println(s"session $sessionId task $currentTask with $cause")
  }

  def onCancellation(sessionId: SessionId, reason: String, variables: ReadableExecutionContext, currentTask: Option[String]): Unit = {
    println(s"session $sessionId was cancelled in task $currentTask")
  }

  def onToRetry(sessionId: SessionId, executionContext: ReadableExecutionContext, currentTask: String, cause: Throwable, taskAttempts: TaskAttempts): Unit = {
    println(s"session $sessionId in task $currentTask is going to retry ${taskAttempts.nextRetry}")
  }

}
