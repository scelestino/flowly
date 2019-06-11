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

import flowly.core.events.EventHook
import flowly.core.repository.InMemoryRepository
import flowly.core.tasks._
import flowly.core.variables.Key
import flowly.core.{DummyEventHook, Workflow}

object Main extends App {

  trait Finish1Component {
    lazy val finish = FinishTask("FINISH 1")
  }

  trait Finish2Component {
    lazy val finish2 = FinishTask("FINISH 2")
  }

  trait BlockingComponent {
    this: Finish1Component =>
    lazy val blocking = BlockingTask("BLOCKING", finish, _.contains(Key3), List(Key3))
  }

  trait SecondComponent {
    this: BlockingComponent =>
    lazy val second: Task = ExecutionTask("EXECUTING 2", blocking) { (sessionId, variables) =>
      println(variables.get(Key1))
      Right(variables.set(Key2, 1234))
    }
  }

  trait BlockingDisjunctionComponent {
    this: Finish2Component with DisjunctionComponent =>
    lazy val blockingDisjunction: Task = BlockingDisjunctionTask("Disjunction", List(Key5, Key6), (_.contains(Key5), disjunction), (_.contains(Key6), finish2))
  }

  trait DisjunctionComponent {
    this: Finish2Component with SecondComponent =>
    lazy val disjunction: Task = DisjunctionTask("Disjunction", finish2, second, _.contains(Key4))
  }

  trait FirstComponent {
    this: BlockingDisjunctionComponent =>
    lazy val first: Task = ExecutionTask("EXECUTING 1", blockingDisjunction) { (sessionId, variables) =>
      Right(variables.set(Key1, "foo bar baz"))
    }
  }

  object Components extends FirstComponent with SecondComponent with DisjunctionComponent with BlockingDisjunctionComponent with BlockingComponent with Finish1Component with Finish2Component

  val workflow = new Workflow {
    def initialTask: Task = Components.first
    def eventHook: EventHook = new DummyEventHook
    override val repository = new InMemoryRepository
  }

  // create and execute a workflow
  val result = for {

    sessionId <- workflow.init()

    result <- workflow.execute(sessionId) // blocked

    _ = println(s"the result is $result\n")

    result2 <- workflow.execute(sessionId)

    _ = println(s"the result is $result2\n")

    result4 <- workflow.execute(sessionId, Key3 -> true)//FakeKey -> Test("a",1))

  } yield result4

  result match {

    case Right(r) => val v: Boolean = r.variables.get(Key3).get
                     println(s"THE RESULT CONTAINS ${r.variables}")
    case Left(ex) => println(ex)
  }

}

case object Key1 extends Key[String]
case object Key2 extends Key[Int]
case object Key3 extends Key[Boolean]
case object Key4 extends Key[Boolean]
case object Key5 extends Key[Int]
case object Key6 extends Key[Int]
/*case object FakeKey extends Key[Test] {
  override def identifier = "Key3"
}

case class Test(a: String, b: Int)*/
