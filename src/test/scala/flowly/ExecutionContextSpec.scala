package flowly

import flowly.context.{ExecutionContext, Key}
import org.scalatest.{FlatSpec, Matchers}

class ExecutionContextSpec extends FlatSpec with Matchers {

  case object Key1 extends Key[String]

  "ExecutionContext" should "be immutable" in {

    val ctx = new ExecutionContext("1", Map.empty)

    ctx.set(Key1, "value1")

    ctx.contains(Key1) shouldBe false

  }

  it should "set new values" in {

    val ctx = new ExecutionContext("1", Map.empty)

    val ctx2 = ctx.set(Key1, "value1")

    ctx2.contains(Key1) shouldBe true

  }

  it should "unset values" in {

    val ctx = new ExecutionContext("1", Map("key1" -> "value1"))

    val ctx2 = ctx.unset(Key1)

    ctx2.contains(Key1) shouldBe false

  }

}
