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

package flowly.tasks.context

import flowly.repository.model.Session
import flowly.tasks.{BlockingTask, DisjunctionTask, ExecutionTask}

/**
  * Interface used by [[BlockingTask]] and [[DisjunctionTask]]
  */
// TODO: change name, we can use this object at [[Result]]
trait ReadableTaskContext {
  this: { val variables: Map[String, Any] } =>

  def sessionId: String

  def get[T](key: Key[T]): Option[T] = variables.get(key.identifier).asInstanceOf[Option[T]]

  def getOrElse[T](key: Key[T], orElse: => T): T = get(key).getOrElse(orElse)

  def contains(key: Key[_]): Boolean = variables.contains(key.identifier)

  def exists[T](key: Key[T], f: T => Boolean): Boolean = get(key).exists(f)

}

/**
  * Interface used by [[ExecutionTask]]
  */
trait WriteableTaskContext {

  def set[T](key: Key[T], value: T): WriteableTaskContext

  def unset(key: Key[_]): WriteableTaskContext

}

/**
  * Immutable context that is pass through the tasks during a workflow execution.
  * It can be used from a [[BlockingTask]], [[DisjunctionTask]] or [[ExecutionTask]] in order to make decisions or
  * it can be modified (through a copy) from an [[ExecutionTask]].
  *
  * After a successful task this context is saved in the repository in order to keep safe any modification.
  *
  * @param sessionId workflow instance id
  * @param variables variables stored in the workflow
  */
class TaskContext private(val sessionId: String, val variables: Map[String, Any]) extends ReadableTaskContext with WriteableTaskContext {

  def set[T](key: Key[T], value: T): TaskContext = new TaskContext(sessionId, variables.updated(key.identifier, value))

  def unset(key: Key[_]): TaskContext = new TaskContext(sessionId, variables.filterKeys(_ != key.identifier))

}

object TaskContext {
  private[flowly] def apply(session: Session): TaskContext = new TaskContext(session.id, session.variables)
}