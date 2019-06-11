package flowly.core.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import flowly.core.SerializationException
import scala.reflect.runtime.universe.TypeTag

class Serializer(objectMapper: ObjectMapper with ScalaObjectMapper) {

  def write(obj: Any): String = {
    try {
      objectMapper.writeValueAsString(obj)
    } catch {
      case cause: Throwable => throw SerializationException(s"Error trying to serialize $obj", cause)
    }
  }

  def read[T: TypeTag](value: String): T = {
    try {
      objectMapper.readValue[T](value)
    } catch {
      case cause: Throwable => throw SerializationException(s"Error trying to deserialize $value", cause)
    }
  }

  def deepCopy[T: TypeTag](obj: Any): T = read(write(obj))

}
