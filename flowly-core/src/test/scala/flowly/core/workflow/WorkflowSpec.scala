package flowly.core.workflow

import flowly.core.context.ExecutionContextFactory
import flowly.core.repository.InMemoryRepository
import flowly.core.tasks.ExecutionTask
import flowly.core.tasks.basic.{FinishTask, Task}
import flowly.core.{Context, Workflow, tasks}
import org.specs2.mutable.Specification

class WorkflowSpec extends Specification {

  "Workflow" should {

    "throw runtime exception" in new Context {
      val task2 = ExecutionTask("Task2", FinishTask("Task2")) { case (_, ec) => Right(ec) }
      val task1 = tasks.ExecutionTask("Task1", task2) { case (_, ec) => Right(ec) }

      new Workflow {
          def initialTask: Task = task1
          override val executionContextFactory = new ExecutionContextFactory(serializer)
          override val repository = new InMemoryRepository
        } must throwAn[IllegalStateException]
    }

  }

}

