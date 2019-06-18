package flowly.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import flowly.core.serialization.{JacksonSerializer, Serializer}
import org.specs2.specification.Scope

trait Context extends Scope {
  lazy val objectMapper: ObjectMapper with ScalaObjectMapper = {
    val om = new ObjectMapper() with ScalaObjectMapper
    om.registerModule(new DefaultScalaModule)
    om
  }
  lazy val serializer: Serializer = new JacksonSerializer(objectMapper)
}
