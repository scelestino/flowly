package flowly.mongodb

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.mongodb.MongoClient
import com.mongodb.client.model.IndexOptions
import flowly.core.repository.Repository
import flowly.core.repository.model.Session
import flowly.core.repository.model.Session.SessionId
import flowly.core.{ErrorOr, SessionNotFound}
import javax.persistence.{OptimisticLockException, PersistenceException}
import org.bson.Document
import org.mongojack.JacksonMongoCollection

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class MongoDBRepository(
                         client: MongoClient,
                         databaseName: String,
                         collectionName: String,
                         objectMapper: ObjectMapper with ScalaObjectMapper
                       ) extends Repository {

  private val om = {
    // Configure Object Mapper in order to work with Session
    objectMapper.registerModule(new DefaultScalaModule)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    objectMapper
  }

  private val collection: JacksonMongoCollection[Session] = {
    val mongoCollection = client.getDatabase(databaseName).getCollection(collectionName)

    val builder: JacksonMongoCollection.JacksonMongoCollectionBuilder[Session] = JacksonMongoCollection.builder()
    val coll = builder.withObjectMapper(om).build(mongoCollection, classOf[Session])

    // Initialize sessionId index
    coll.createIndex(new Document("sessionId", 1), new IndexOptions().unique(true))
    coll
  }

  override def getSession(sessionId: SessionId): ErrorOr[Session] = {
    Try(collection.findOne(new Document("sessionId", sessionId))) match {
      case Success(null) => Left(SessionNotFound(sessionId))
      case Success(element) => Right(element)
      case Failure(throwable) => Left(new PersistenceException(s"Error getting session $sessionId", throwable))
    }
  }

  override def insertSession(session: Session): ErrorOr[Session] = {
    Try(collection.insert(session)) match {
      case Success(_) => Right(session)
      case Failure(throwable) => Left(new PersistenceException(s"Error inserting session ${session.sessionId}", throwable))
    }
  }

  override def updateSession(session: Session): ErrorOr[Session] = {
    Try {
      // Update will replace every document field and it is going to increment in one unit its version
      val document = JacksonMongoCollection.convertToDocument(session, om, classOf[Session])
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