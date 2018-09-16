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

package flowly

import flowly.events.EventHook
import flowly.tasks._
import flowly.variables.Key

object Main extends App {

  trait Finish1Component {
    lazy val finish = FinishTask("FINISH 1")
  }

  trait Finish2Component {
    lazy val finish2 = FinishTask("FINISH 2")
  }

  trait BlockingComponent {
    this: Finish1Component =>
    lazy val blocking = BlockingTask("BLOCKING", finish, _.contains(Key3))
  }

  trait SecondComponent {
    this: BlockingComponent =>
    lazy val second: Task = ExecutionTask("EXECUTING 2", blocking) { (sessionId, variables) =>
      println(variables.get(Key1))
      Right(variables.set(Key2, 1234))
    }
  }

  trait DisjunctionComponent {
    this: Finish2Component with SecondComponent =>
    lazy val disjunction: Task = DisjunctionTask("Disjunction", finish2, second, _.contains(Key4))
  }

  trait FirstComponent {
    this: DisjunctionComponent =>
    lazy val first: Task = ExecutionTask("EXECUTING 1", disjunction) { (sessionId, variables) =>
      Right(variables.set(Key1, "foo bar baz"))
    }
  }

  object Components extends FirstComponent with SecondComponent with DisjunctionComponent with BlockingComponent with Finish1Component with Finish2Component

  val workflow = new Workflow {
    def initialTask: Task = Components.first
    def eventHook: EventHook = new DummyEventHook
  }

  val result = for {

    sessionId <- workflow.init()

    result <- workflow.execute(sessionId)

    _ = println(s"the result is $result\n")

    result2 <- workflow.execute(sessionId, Key3 -> false)

  } yield result2

  result match {
    case Right(r) => println(s"THE RESULT CONTAINS ${r.variables}")
    case Left(ex) => println(ex)
  }

}

case object Key1 extends Key[String]

case object Key2 extends Key[Int]

case object Key3 extends Key[Boolean]

case object Key4 extends Key[Boolean]



object Main4 extends App {

//  // I can't change this code
//  def foo:String = throw new RuntimeException("foo method fails")
//  def bar:String = throw new RuntimeException("bar method fails")
//
//  // my code
//  try {
//    foo
//  } catch {
//    case error1:Throwable =>
//      try {
//        // if foo fails, I want to call bar
//        bar
//        throw new RuntimeException("my exception", error1)
//      } catch {
//        case error2:Throwable =>
//          // but bar could fail too
//          throw new RuntimeException("my exception", ???)
//          // if my cause is error1, I lost error2 information (message and stack)
//          // if my cause is error2, I lost error1 information (message and stack)
//      }
//  }
//



}