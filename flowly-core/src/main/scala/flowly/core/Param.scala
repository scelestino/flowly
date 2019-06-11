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

import flowly.core.variables.{Key, ExecutionContext}

import scala.language.implicitConversions

/**
  * Object used to ensure the relationship between keys and values at variable arguments methods
  *
  */
class Param private(val key: String, val value: Any)

object Param {

  def apply[A](key: Key[A], value: A): Param = new Param(key.identifier, value)

  def unapply(param: Param): Option[(String, Any)] = Some(param.key -> param.value)

  implicit def tuple2Param[A](keyValue: (Key[A], A)): Param = keyValue match {
    case (key, value) => Param(key, value)
  }

  implicit class ParamSeqOps(params: Seq[Param]) {
    private[flowly] def toVariables: Map[String, Any] = params.map(p => (p.key, p.value)).toMap
  }

}
