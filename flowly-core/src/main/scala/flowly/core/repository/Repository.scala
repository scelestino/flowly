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

package flowly.core.repository

import flowly.core.{ErrorOr, SessionNotFound}
import flowly.core.repository.model.Session
import flowly.core.repository.model.Session.SessionId

import scala.collection.mutable


trait Repository {
  def insertSession(session: Session): ErrorOr[Session]
  def getSession(sessionId: SessionId): ErrorOr[Session]
  def updateSession(session: Session): ErrorOr[Session]
}

// dummy repository
class InMemoryRepository extends Repository {

  private val storage: mutable.Map[String, Session] = mutable.Map[String, Session]()

  override def insertSession(session: Session): ErrorOr[Session] = {
    updateSession(session)
  }

  override def getSession(sessionId: SessionId): ErrorOr[Session] = {
    storage.get(sessionId).toRight(SessionNotFound(sessionId))
  }

  override def updateSession(session: Session): ErrorOr[Session] = {
    storage.update(session.sessionId, session)
    Right(session)
  }

}