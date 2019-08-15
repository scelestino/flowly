package flowly.mongodb

import java.time.Instant
import java.util.Date

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{DeserializationContext, JsonDeserializer, JsonSerializer, SerializerProvider}
import org.mongojack.internal.`object`.document.DocumentObjectGenerator

object CustomDateModule extends SimpleModule("CustomDateModule") {
  this.addDeserializer(classOf[Instant], InstantJsonDeserializer)
  this.addSerializer(classOf[Instant], InstantJsonSerializer)
  this.addSerializer(classOf[Date], DateJsonSerializer)
}

object InstantJsonSerializer extends JsonSerializer[Instant] {
  def serialize(value: Instant, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
    jgen.writeObject(Date.from(value))
  }
}

object InstantJsonDeserializer extends JsonDeserializer[Instant] {
  def deserialize(jp: JsonParser, ctx: DeserializationContext): Instant = {
    jp.getEmbeddedObject.asInstanceOf[Date].toInstant
  }
}

object DateJsonSerializer extends JsonSerializer[Date] {
  def serialize(value: Date, jgen: JsonGenerator, serializers: SerializerProvider): Unit = {
    jgen match {
      case _: DocumentObjectGenerator => jgen.writeObject(value)
      case _ => jgen.writeString(value.toString)
    }
  }
}

