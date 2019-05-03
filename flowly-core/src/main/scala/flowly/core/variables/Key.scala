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


/**
  * Interface used to get access to objects inside [[Variables]], it creates a relation
  * between an identifier and a type.
  *
  * {{{
  *
  *   case object Key1 extends Key[String]
  *   case object Key2 extends Key[Int]
  *   case object Key3 extends Key[List[Foo]]
  *
  *   Variables(Map.empty).set(Key2, 123)
  *
  * }}}
  *
  * @tparam A what kind of object can be used with this key
  */
trait Key[A] {
  this: Product =>
  def identifier: String = this.toString
}
