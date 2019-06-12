package flowly.core.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class SerializerSpec extends Specification {

  "Serializer" should {

    "serialize" in new Context {
      val obj = new Container(List(new Fruta[Int](1, "pera"), new Fruta[Int](2, "manzana")))
      serializer.write(obj) must be equalTo "{\"obj\":[{\"id\":1,\"name\":\"pera\"},{\"id\":2,\"name\":\"manzana\"}]}"
    }

    "deserialize" in new Context {
      val container = serializer.read[Container[List[Fruta[Int]]]]("{\"obj\":[{\"id\":1,\"name\":\"pera\"},{\"id\":2,\"name\":\"manzana\"}]}")
      val obj = container.obj

      obj must haveSize(2)

      val first: Fruta[Int] = obj.head
      first.id must be equalTo 1
      first.name must be equalTo "pera"

      val second: Fruta[Int] = obj(1)
      second.id must be equalTo 2
      second.name must be equalTo "manzana"
    }

    "deepCopy a complex object" in new Context {
      val original = new Container(List(new Fruta[Int](1, "pera"), new Fruta[Int](2, "manzana")))
      val result = serializer.deepCopy[Container[List[Fruta[Int]]]](original)

      result.isInstanceOf[Container[_]] must be equalTo true
      result.obj.isInstanceOf[List[_]] must be equalTo true
      result.obj must haveSize(2)

      val first: Fruta[Int] = result.obj.head
      first.id must be equalTo 1
      first.name must be equalTo "pera"

      val second: Fruta[Int] = result.obj(1)
      second.id must be equalTo 2
      second.name must be equalTo "manzana"
    }

    "deepCopy a native String" in new Context {
      serializer.deepCopy[String]("string rules") must be equalTo "string rules"
    }

    "deepCopy a native Int" in new Context {
      serializer.deepCopy[Int](123) must be equalTo 123
    }

  }

}

class Container[T](val obj: T)
class Fruta[T](val id: T, val name: String)

trait Context extends Scope with ScalaObjectMapperContext {
  lazy val serializer: Serializer = new Serializer(objectMapper)
}

trait ScalaObjectMapperContext {
  lazy val objectMapper: ObjectMapper with ScalaObjectMapper = {
    val om = new ObjectMapper() with ScalaObjectMapper
    om.registerModule(new DefaultScalaModule)
    om
  }
}