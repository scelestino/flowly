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

package flowly.variables

import flowly.tasks._

/**
  * Read-only interface of Variables
  */
trait ReadableVariables {

  def get[T](key: Key[T]): Option[T]

  def getOrElse[T](key: Key[T], orElse: => T): T

  def contains(key: Key[_]): Boolean

  def exists[T](key: Key[T], f: T => Boolean): Boolean

}

/**
  * Immutable variables that are pass through the tasks during a workflow execution.
  * It can be used from a [[BlockingTask]], [[DisjunctionTask]] or [[ExecutionTask]] in order to make decisions or
  * it can be modified (through a copy) from an [[ExecutionTask]].
  *
  * After a successful task this context is saved in the repository in order to keep safe any modification.
  *
  * @param underlying variables storage
  */
class Variables private (underlying:Map[String, Any]) extends ReadableVariables {

  def get[T](key: Key[T]): Option[T] = underlying.get(key.identifier).asInstanceOf[Option[T]]

  def getOrElse[T](key: Key[T], orElse: => T): T = get(key).getOrElse(orElse)

  def contains(key: Key[_]): Boolean = underlying.contains(key.identifier)

  def exists[T](key: Key[T], f: T => Boolean): Boolean = get(key).exists(f)

  def set[T](key: Key[T], value: T): Variables = new Variables(underlying.updated(key.identifier, value))

  def unset(key: Key[_]): Variables = new Variables(underlying.filterKeys(_ != key.identifier))

  /**
    * It returns raw values from Variables
    *
    * @return Map
    */
  private[flowly] def values:Map[String, Any] = underlying

  /**
    * Merge two Variables content (second overrides first one)
    *
    * @param variables another Variables object
    * @return result between merging
    */
  private[flowly] def merge(variables:Variables):Variables = Variables(underlying ++ variables.values)

}

object Variables {

  private[flowly] def apply(underlying: Map[String, Any]): Variables = new Variables(underlying)

}