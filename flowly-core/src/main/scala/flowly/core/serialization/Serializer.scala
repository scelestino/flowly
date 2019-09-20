package flowly.core.serialization

trait Serializer {
  def write(obj: Any): String
  def read[T: Manifest](value: String): T
  final def deepCopy[T: Manifest](obj: Any): T = read[T](write(obj))
}