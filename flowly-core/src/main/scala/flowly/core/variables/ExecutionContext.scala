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

package flowly.core.variables

import flowly.core.repository.model.Session
import flowly.core.repository.model.Session.SessionId
import flowly.core.serialization.Serializer
import flowly.core.tasks._

import scala.reflect.runtime.universe.TypeTag

/**
  * Read-only interface of Variables
  */
trait ReadableExecutionContext {

  def get[T: TypeTag](key: Key[T]): Option[T]

  def getOrElse[T: TypeTag](key: Key[T], orElse: => T): T

  def contains(key: Key[_]): Boolean

  def exists[T: TypeTag](key: Key[T], f: T => Boolean): Boolean

  def vars: Map[String, Any]

}

/**
  * Immutable variables that are pass through the tasks during a workflow execution.
  * It can be used from a [[BlockingTask]], [[DisjunctionTask]] or [[ExecutionTask]] in order to make decisions or
  * it can be modified (through a copy) from an [[ExecutionTask]].
  *
  * After a successful task this context is saved in the repository in order to keep safe any modification.
  *
  * @param variables variables storage
  */
class ExecutionContext private[flowly](sessionId: SessionId, private[flowly] val variables: Map[String, Any], serializer: Serializer) extends ReadableExecutionContext {

  def get[T: TypeTag](key: Key[T]): Option[T] = variables.get(key.identifier).map(_.asInstanceOf[T]).map(serializer.deepCopy[T])

  def getOrElse[T: TypeTag](key: Key[T], orElse: => T): T = get(key).getOrElse(orElse)

  def contains(key: Key[_]): Boolean = variables.contains(key.identifier)

  def exists[T: TypeTag](key: Key[T], f: T => Boolean): Boolean = get(key).exists(f)

  def set[T: TypeTag](key: Key[T], value: T): ExecutionContext = new ExecutionContext(sessionId, variables.updated(key.identifier, serializer.deepCopy(value)), serializer)

  def unset(key: Key[_]): ExecutionContext = new ExecutionContext(sessionId, variables.filterKeys(_ != key.identifier), serializer)

  def vars: Map[String, Any] = serializer.deepCopy(variables)

  /**
    * Merge two Variables content (second overrides first one)
    *
    * @param variables another Variables object
    * @return result of merging between both variables
    */
  private[flowly] def merge(variables: Map[String, Any]): ExecutionContext = new ExecutionContext(sessionId: SessionId, variables ++ variables, serializer)

}

class ExecutionContextFactory(serializer: Serializer) {
  def create(session: Session): ExecutionContext = new ExecutionContext(session.sessionId, session.variables, serializer)
}