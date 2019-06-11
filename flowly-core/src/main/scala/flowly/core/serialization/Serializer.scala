package flowly.core.serialization

import java.lang.reflect.{ParameterizedType, Type => JType}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import flowly.core.SerializationException

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

class Serializer(objectMapper: ObjectMapper) {

  def write(obj: Any): String = {
    try {
      objectMapper.writeValueAsString(obj)
    } catch {
      case cause: Throwable => throw SerializationException(s"Error trying to serialize $obj", cause)
    }
  }

  def read[T](value: String)(implicit tag: TypeTag[T]): T = {
    try {
      objectMapper.readValue(value, typeReference[T])
    } catch {
      case cause: Throwable => throw SerializationException(s"Error trying to deserialize $value", cause)
    }
  }

  def deepCopy[T: TypeTag](obj: Any): T = read(write(obj))

  private def typeReference[T](implicit tag: TypeTag[T]) = new TypeReference[T] {
    override val getType: JType = jType(tag.tpe)
  }

  private def jType(typ: Type): JType =
    synchronized {
      typ match {
        case TypeRef(_, sig, args) =>
          new ParameterizedType {
            val getRawType = currentMirror.runtimeClass(sig.asType.toType)
            val getActualTypeArguments = args.map(jType).toArray
            val getOwnerType = null
          }
      }
    }

}
