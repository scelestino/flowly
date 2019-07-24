package flowly.core.workflow

import flowly.core.repository.InMemoryRepository
import flowly.core.tasks.basic.{ExecutionTask, FinishTask, Task}
import flowly.core.variables.ExecutionContextFactory
import flowly.core.{Context, Workflow}
import org.specs2.mutable.Specification

class WorkflowSpec extends Specification {

  "Workflow" should {

    "throw runtime exception" in new Context {
      val task2 = ExecutionTask("Task2", FinishTask("Task2")) { case (_, ec) => Right(ec) }
      val task1 = ExecutionTask("Task1", task2) { case (_, ec) => Right(ec) }

      new Workflow {
          def initialTask: Task = task1
          override val executionContextFactory = new ExecutionContextFactory(serializer)
          override val repository = new InMemoryRepository
        } must throwAn[IllegalStateException]
    }
  }
}

