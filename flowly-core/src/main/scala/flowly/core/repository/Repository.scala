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
import flowly.core.repository.model.Session.{SessionId, Status}

import scala.collection.mutable


trait Repository {
  def insert(session: Session): ErrorOr[Session]
  def getById(sessionId: SessionId): ErrorOr[Session]
  def getByStatus(status: Status): ErrorOr[List[SessionId]]
  def update(session: Session): ErrorOr[Session]
}

// dummy repository
class InMemoryRepository extends Repository {

  private val storage: mutable.Map[String, Session] = mutable.Map[String, Session]()

  def insert(session: Session): ErrorOr[Session] = {
    update(session)
  }

  def update(session: Session): ErrorOr[Session] = {
    storage.update(session.sessionId, session)
    Right(session)
  }

  def getById(sessionId: SessionId): ErrorOr[Session] = {
    storage.get(sessionId).toRight(SessionNotFound(sessionId))
  }

  def getByStatus(status: Status): ErrorOr[List[SessionId]] = {
    Right(storage.values.filter(_.status == status).map(_.sessionId).toList)
  }

}