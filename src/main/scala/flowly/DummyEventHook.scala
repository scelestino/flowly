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

import flowly.events.EventHook
import flowly.repository.model.Session.SessionId
import flowly.variables.ReadableVariables

class DummyEventHook extends EventHook {

  def onInitialization(sessionId: SessionId, variables: ReadableVariables): Unit = {
    println(s"Init a new instance $sessionId")
  }

  def onStart(sessionId: SessionId, variables: ReadableVariables): Unit = {
    println(s"session $sessionId just started")
  }

  def onExecution(sessionId: SessionId, variables: ReadableVariables, currentTask: String): Unit = {
    println(s"session $sessionId task $currentTask executed")
  }

  def onBlocked(sessionId: SessionId, variables: ReadableVariables, currentTask: String): Unit = {
    println(s"session $sessionId task $currentTask blocked")
  }

  def onFinish(sessionId: SessionId, variables: ReadableVariables, currentTask: String): Unit = {
    println(s"session $sessionId just finished")
  }

  def onError(sessionId: SessionId, variables: ReadableVariables, currentTask: String, cause:Throwable): Unit = {
    println(s"session $sessionId task $currentTask with $cause")
  }

  def onCancellation(sessionId: SessionId, reason: String, variables: ReadableVariables, currentTask: Option[String]): Unit = {
    println(s"session $sessionId was cancelled in task $currentTask")
  }

}
