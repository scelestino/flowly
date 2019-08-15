package flowly.mongodb

import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.mongodb.MongoClient
import com.mongodb.client.model.IndexOptions
import flowly.core.repository.Repository
import flowly.core.repository.model.Session
import flowly.core.repository.model.Session.{SessionId, Status}
import flowly.core.{ErrorOr, SessionNotFound}
import javax.persistence.{OptimisticLockException, PersistenceException}
import org.bson.Document
import org.mongojack.JacksonMongoCollection

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class MongoDBRepository(client: MongoClient, databaseName: String, collectionName: String, objectMapper: ObjectMapper with ScalaObjectMapper) extends Repository {

  private val collection: JacksonMongoCollection[Session] = {
    val mongoCollection = client.getDatabase(databaseName).getCollection(collectionName)

    val builder: JacksonMongoCollection.JacksonMongoCollectionBuilder[Session] = JacksonMongoCollection.builder()
    val coll = builder.withObjectMapper(objectMapper).build(mongoCollection, classOf[Session])

    // Initialize sessionId index
    coll.createIndex(new Document("sessionId", 1), new IndexOptions().unique(true))
    coll
  }

  def getById(sessionId: SessionId): ErrorOr[Session] = {
    Try(collection.findOne(new Document("sessionId", sessionId))) match {
      case Success(null) => Left(SessionNotFound(sessionId))
      case Success(element) => Right(element)
      case Failure(throwable) => Left(new PersistenceException(s"Error getting session $sessionId", throwable))
    }
  }

  def getByStatus(status: Status): ErrorOr[List[SessionId]] = {
    Try(collection.find(new Document("status", status))) match {
      case Success(result) => Right(result.map(_.sessionId).asScala.toList)
      case Failure(throwable) => Left(new PersistenceException(s"Error getting sessions by status $status", throwable))
    }
  }

  def insert(session: Session): ErrorOr[Session] = {
    Try(collection.insert(session)) match {
      case Success(_) => Right(session)
      case Failure(throwable) => Left(new PersistenceException(s"Error inserting session ${session.sessionId}", throwable))
    }
  }

  def update(session: Session): ErrorOr[Session] = {
    Try {
      // Update will replace every document field and it is going to increment in one unit its version
      val document = JacksonMongoCollection.convertToDocument(session, objectMapper, classOf[Session])
      document.remove("version")

      val update = new Document("$set", document)
      update.put("$inc", new Document("version", 1))

      // Condition: there is a session with the same sessionId and version
      val query = Map[String, AnyRef](
        "sessionId" -> session.sessionId,
        "version" -> java.lang.Long.valueOf(session.version)
      ).asJava

      collection.findAndModify(new Document(query), new Document(), new Document(), collection.serializeFields(update), true, false)

    } match {
      case Success(null) => Left(new OptimisticLockException(s"Session ${session.sessionId} was modified by another transaction"))
      case Success(elem) => Right(elem)
      case Failure(throwable) => Left(throwable)
    }
  }

}