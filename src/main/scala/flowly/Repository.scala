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

import flowly.session.Session
import java.time.LocalDateTime

import scala.collection.mutable

// dummy repo
class Repository {

  private val storage = mutable.Map[String, Session]("1" -> Session("1", None, Map.empty, LocalDateTime.now))

  def getSession(sessionId:String):Option[Session] = storage.get(sessionId)

  def saveSession(session: Session):Unit = {
    storage.update(session.id, session)
  }

}
